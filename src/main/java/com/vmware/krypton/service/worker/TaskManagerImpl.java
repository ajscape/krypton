package com.vmware.krypton.service.worker;

import static com.vmware.krypton.util.XenonUtil.sendOperation;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.vmware.krypton.document.worker.TaskDoc;
import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskCompletionEvent;
import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskData;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.krypton.repository.worker.TaskDocRepository;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceDocumentQueryResult;
import com.vmware.xenon.common.ServiceHost;
import com.vmware.xenon.common.Utils;

public class TaskManagerImpl implements TaskManager {

    private ServiceHost host;
    private TaskExecutor taskExecutor;
    private String workerId;
    private Map<String, String> taskIdToWorkerIdMap= new HashMap<>();
    private Map<String, String> workerIdToHostnameMap = new HashMap<>();
    private Map<String, Task> taskNameToTaskMap = new HashMap<>();

    public TaskManagerImpl(ServiceHost host, TaskExecutor taskExecutor) {
        this.host = host;
        workerId = host.getId();
    }

    @Override
    public CompletableFuture<Void> receiveWorkerTaskSchedule(WorkerTaskSchedule schedule) {
        taskIdToWorkerIdMap.putAll(schedule.getTaskIdToWorkerIdMap());
        workerIdToHostnameMap.putAll(schedule.getWorkerIdToHostnameMap());

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        schedule.getTaskDescriptions().forEach(taskDescription -> {
            TaskDoc taskDoc = taskDescriptionToTaskDoc(taskDescription);
            futures.add(postTaskDoc(taskDoc));
        });
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
    }

    @Override
    public CompletableFuture<Void> receiveTaskInput(WorkerTaskData taskInput) {
        return getTaskDoc(taskInput.getDstTaskId()).thenCompose(taskDoc -> {
            Object inputData = taskInput.getData();
            if(taskInput.isSrcTaskCompletionEvent()) {
                inputData = new TaskCompletionEvent();
            }
            taskDoc.inputTaskIdToDataMap.put(taskInput.getSrcTaskId(), inputData);
            return postTaskDoc(taskDoc).thenCompose(aVoid -> {
                //check if all inputs present
                if(taskDoc.inputTaskIdToDataMap.keySet().size() == taskDoc.inputTaskIds.size()) {
                    //if yes, check if there is any real data input
                    if(getInputTaskDataMap(taskDoc).isEmpty()) {
                        //if input data map empty, all previous tasks have completed
                        //mark current task as complete
                        return updateTaskState(taskInput.getDstTaskId(), TaskState.COMPLETED);
                    } else {
                        //else, schedule the task for execution
                        Task task = taskNameToTaskMap.get(taskDoc.taskName);
                        TaskContext taskContext = taskDocToTaskContext(taskDoc);
                        taskExecutor.executeTask(task, taskContext);
                        return CompletableFuture.completedFuture(null);
                    }
                }
                return CompletableFuture.completedFuture(null);
            });
        });
    }

    @Override
    public CompletableFuture<Void> sendTaskOutput(WorkerTaskData taskOutput) {
        URI uri = URI.create(taskIdToHostname(taskOutput.getDstTaskId()) + "/krypton/worker/task-input");
        Operation op = Operation.createPost(uri).setBody(taskOutput);
        return sendOperation(host, op, null);
    }

    @Override
    public CompletableFuture<Void> sendTaskCompletionEvent(String srcTaskId, String dstTaskId) {
        return null;
    }

    @Override
    public CompletableFuture<Map<String, TaskState>> getAllTasksStates() {
        return getAllTaskDocs().thenApply(taskDocs -> {
            return taskDocs.stream().collect(Collectors.toMap(d -> d.taskId, d -> d.taskState));
        });
    }

    @Override
    public CompletableFuture<Void> updateTaskState(String taskId, TaskState taskState) {
        if (taskState == TaskState.COMPLETED) {
            return handleTaskComplete(taskId);
        } else {
            TaskDoc taskDoc = new TaskDoc();
            taskDoc.taskState = taskState;

            if(taskState == TaskState.PARTIAL_COMPLETED) {
                taskDoc.inputTaskIdToDataMap.forEach((k, v) -> {
                    if(v instanceof TaskCompletionEvent) {
                        //skip
                    } else {
                        taskDoc.inputTaskIdToDataMap.remove(k);
                    }
                });
            }

            Operation updateStateOp = Operation.createPatch(host, taskIdToSelfLink(taskId))
                    .setBody(taskDoc);
            return sendOperation(host, updateStateOp, null);
        }
    }

    @Override
    public ServiceHost getHost() {
        return host;
    }

    private CompletableFuture<Void> postTaskDoc(TaskDoc taskDoc) {
        Operation op = Operation.createPost(host, TaskDocRepository.FACTORY_LINK)
                .setBody(taskDoc);
        return sendOperation(host, op, null);
    }

    private CompletableFuture<TaskDoc> getTaskDoc(String taskId) {
        Operation op = Operation.createGet(host, taskIdToSelfLink(taskId));
        return sendOperation(host, op, TaskDoc.class);
    }

    private CompletableFuture<List<TaskDoc>> getAllTaskDocs() {
        Operation op = Operation.createGet(host, TaskDocRepository.FACTORY_LINK + "?expand");
        return sendOperation(host, op, ServiceDocumentQueryResult.class).thenApply(result -> {
            List<TaskDoc> taskDocs = new ArrayList<>();
            result.documents.forEach((selfLink, document) -> {
                taskDocs.add(Utils.fromJson(document, TaskDoc.class));
            });
            return taskDocs;
        });
    }

    private CompletableFuture<Void> deleteTaskDoc(String taskId) {
        Operation op = Operation.createDelete(host, taskIdToSelfLink(taskId));
        return sendOperation(host, op, null);
    }

    private static TaskDoc taskDescriptionToTaskDoc(TaskDescription taskDescription) {
        TaskDoc taskDoc = new TaskDoc();
        taskDoc.taskId = taskDescription.getTaskId();
        taskDoc.taskName = taskDescription.getTaskName();
        taskDoc.inputTaskIds = taskDescription.getInputTaskIds();
        taskDoc.outputTaskIds = taskDescription.getOutputTaskIds();
        taskDoc.inputTaskIdToDataMap = new HashMap<>();
        taskDoc.taskState = TaskState.WAITING;
        taskDoc.documentSelfLink = taskDoc.taskId;
        return taskDoc;
    }

    private static TaskDescription taskDocToTaskDescription(TaskDoc taskDoc) {
        TaskDescription taskDescription = new TaskDescription(taskDoc.taskId, taskDoc.taskName);
        taskDescription.setInputTaskIds(taskDoc.inputTaskIds);
        taskDescription.setOutputTaskIds(taskDoc.outputTaskIds);
        return taskDescription;
    }

    private String taskIdToHostname(String taskId) {
        String workerId = taskIdToWorkerIdMap.get(taskId);
        return workerIdToHostnameMap.get(workerId);
    }

    private TaskContext taskDocToTaskContext(TaskDoc taskDoc) {
        TaskContext taskContext = new TaskContext();
        taskContext.setTaskManager(this);
        taskContext.setTaskDescription(taskDocToTaskDescription(taskDoc));
        taskContext.setInputTaskIdToDataMap(getInputTaskDataMap(taskDoc));
        return taskContext;
    }

    private Map<String, Object> getInputTaskDataMap(TaskDoc taskDoc) {
        Map taskDataMap = new HashMap();
        taskDoc.inputTaskIdToDataMap.forEach((taskId, data) -> {
            if(data instanceof TaskCompletionEvent) {
                //skip
            } else {
                taskDataMap.put(taskId, data);
            }
        });
        return taskDataMap;
    }

    private static String taskIdToSelfLink(String taskId) {
        return TaskDocRepository.FACTORY_LINK + "/" + taskId;
    }

    private CompletableFuture<Void> handleTaskComplete(String taskId) {
        return getTaskDoc(taskId).thenCompose(taskDoc -> {
            taskDoc.outputTaskIds.forEach(outputTaskId ->
                    sendTaskCompletionEvent(taskId, outputTaskId)
            );
            return deleteTaskDoc(taskId);
        });
    }
}
