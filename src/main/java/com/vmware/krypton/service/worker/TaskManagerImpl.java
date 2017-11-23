package com.vmware.krypton.service.worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.vmware.krypton.document.worker.TaskDoc;
import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskCompletionEvent;
import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.krypton.repository.worker.TaskDocRepository;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceHost;

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
    public CompletableFuture<Void> receiveTaskInput(String srcTaskId, String dstTaskId, Object input) {
        return getTaskDoc(dstTaskId).thenCompose(taskDoc -> {
            taskDoc.inputTaskIdToDataMap.put(srcTaskId, input);
            return postTaskDoc(taskDoc).thenCompose(aVoid -> {
                //check if all inputs present
                if(taskDoc.inputTaskIdToDataMap.keySet().size() == taskDoc.inputTaskIds.size()) {
                    //if yes, check if there is any real data input
                    if(getInputTaskDataMap(taskDoc).isEmpty()) {
                        //if input data map empty, all previous tasks have completed
                        //mark current task as complete
                        return updateTaskState(dstTaskId, TaskState.COMPLETED);
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
    public CompletableFuture<Void> receiveTaskCompletionEvent(String srcTaskId, String dstTaskId) {
        return receiveTaskInput(srcTaskId, dstTaskId, new TaskCompletionEvent());
    }

    @Override
    public CompletableFuture<Void> sendTaskOutput(String srcTaskId, String dstTaskId, Object Output) {
        return null;
    }

    @Override
    public CompletableFuture<Void> sendTaskCompletionEvent(String srcTaskId, String dstTaskId) {
        return null;
    }

    @Override
    public CompletableFuture<Map<String, TaskState>> getAllTasksStates() {
        return null;
    }

    @Override
    public CompletableFuture<Void> updateTaskState(String taskId, TaskState taskState) {
//        switch(taskState) {
//            default:
//                TaskDoc taskDoc = new TaskDoc();
//                taskDoc.taskState = taskState;
//                return Operation.createPatch(host, taskIdToSelfLink(taskId))
//                        .setBody(taskDoc);
//
//        }
        return null;
    }

    @Override
    public ServiceHost getHost() {
        return host;
    }

    private CompletableFuture<Void> postTaskDoc(TaskDoc taskDoc) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Operation.createPost(host, TaskDocRepository.FACTORY_LINK)
                .setBody(taskDoc)
                .setCompletion((o, e) -> {
                    if (e != null) {
                        future.completeExceptionally(e);
                    }
                    future.complete(null);
                }).sendWith(host);
        return future;
    }

    private CompletableFuture<TaskDoc> getTaskDoc(String taskId) {
        Operation op = Operation.createGet(host, taskIdToSelfLink(taskId));
        //return sendOperation(op, TaskDoc.class);
        return null;
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
}
