package com.vmware.krypton.service.worker;

import static com.vmware.krypton.controller.master.JobController.WORKER_SAVE_RESULT;
import static com.vmware.krypton.service.master.JobToTaskGraphTransformerImpl.MASTER_TASK_ID;
import static com.vmware.krypton.util.XenonUtil.sendOperation;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.krypton.controller.master.JobController;
import com.vmware.krypton.controller.master.JobResult;
import com.vmware.krypton.document.worker.TaskDoc;
import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskData;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.krypton.repository.worker.TaskDocRepository;
import com.vmware.krypton.service.mappers.basic.DefaultMapper;
import com.vmware.krypton.service.tasks.Combiner;
import com.vmware.krypton.service.tasks.HelloWorldTask;
import com.vmware.krypton.service.tasks.OdataQueryTask;
import com.vmware.krypton.service.tasks.ReducerTask;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceDocumentQueryResult;
import com.vmware.xenon.common.ServiceHost;
import com.vmware.xenon.common.Utils;

public class TaskManagerImpl implements TaskManager {

    private static final Logger logger = LoggerFactory.getLogger(TaskManagerImpl.class);

    @Inject
    private ServiceHost host;
    @Inject
    private TaskExecutor taskExecutor;
    private Map<String, String> taskIdToHostnameMap = new HashMap<>();
    private Map<String, Task> taskNameToTaskMap = new HashMap<>();

    public TaskManagerImpl() {
        taskNameToTaskMap.put(OdataQueryTask.class.getName(), new OdataQueryTask());
        taskNameToTaskMap.put(DefaultMapper.class.getName(), new DefaultMapper());
        taskNameToTaskMap.put(ReducerTask.class.getName(), new ReducerTask());
        taskNameToTaskMap.put(Combiner.class.getName(), new Combiner());
        taskNameToTaskMap.put("helloWorld", new HelloWorldTask());
    }

    @Override
    public CompletableFuture<Void> receiveWorkerTaskSchedule(WorkerTaskSchedule schedule) {
        logger.info("Received task schedule - {}", schedule.getTaskDescriptions());
        taskIdToHostnameMap.putAll(schedule.getTaskIdToHostnameMap());

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        schedule.getTaskDescriptions().forEach(taskDescription -> {
            TaskDoc taskDoc = taskDescriptionToTaskDoc(taskDescription);
            futures.add(postTaskDoc(taskDoc));
        });
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
    }

    @Override
    public synchronized CompletableFuture<Void> receiveTaskInput(WorkerTaskData taskInput) {
        return getTaskDoc(taskInput.getDstTaskId()).thenCompose(taskDoc -> {
            String inputData = taskInput.getData();
            if (taskInput.isSrcTaskCompleted()) {
                taskDoc.inputTaskIdToCompletionMap.put(taskInput.getSrcTaskId(), true);
                logger.info("{}: Received completionEvent from {}", taskInput.getDstTaskId(), taskInput.getSrcTaskId());
            } else {
                taskDoc.inputTaskIdToDataMap.put(taskInput.getSrcTaskId(), inputData);
                logger.info("{}: Received data from {}", taskInput.getDstTaskId(), taskInput.getSrcTaskId());
            }
            return patchTaskDoc(taskDoc).thenCompose(aVoid -> {
                //check if all inputData tasks completed
                if (taskDoc.inputTaskIdToCompletionMap.keySet().size() == taskDoc.inputTaskIds.size()) {
                    //if yes, mark current task as complete
                    return updateTaskState(taskInput.getDstTaskId(), TaskState.COMPLETED);
                } else if (taskDoc.inputTaskIdToDataMap.keySet().size() == taskDoc.inputTaskIds.size()) {
                    //else if all inputData data present, schedule the task for execution
                    Task task = taskNameToTaskMap.get(taskDoc.taskName);
                    TaskContext taskContext = taskDocToTaskContext(taskDoc);
                    taskExecutor.executeTask(task, taskContext);
                    return CompletableFuture.completedFuture(null);
                } else {
                    // wait for all inputData data to arrive
                }
                return CompletableFuture.completedFuture(null);
            });
        });
    }

    @Override
    public CompletableFuture<Void> sendTaskOutput(WorkerTaskData taskOutput) {
        if(taskOutput.isSrcTaskCompleted()) {
            logger.info("{}: Sending completionEvent to {}", taskOutput.getSrcTaskId(), taskOutput.getDstTaskId());
        } else {
            logger.info("{}: Sending data to {}", taskOutput.getSrcTaskId(), taskOutput.getDstTaskId());
        }
        if(taskOutput.getDstTaskId().equals(MASTER_TASK_ID)) {
            JobResult jobResult = new JobResult(taskOutput.getJobId(), true, true, taskOutput.getData(), null);
            return sendJobResult(jobResult);
        }
        String hostName = taskIdToHostnameMap.get(taskOutput.getDstTaskId());
        if (hostName == null) {
            logger.error("{}: Unable to find hostname for {}", taskOutput.getSrcTaskId(), taskOutput.getDstTaskId());
            JobResult jobResult = new JobResult(taskOutput.getJobId(), true, false, null,
                    "Unable to find hostname for " + taskOutput.getDstTaskId());
            return sendJobResult(jobResult);
        }
        URI uri = URI.create(hostName + "/krypton/worker/task-input");
        Operation op = Operation.createPost(uri).setBody(taskOutput);
        return sendOperation(host, op, null);
    }

    @Override
    public CompletableFuture<Map<String, TaskState>> getAllTasksStates() {
        return getAllTaskDocs().thenApply(taskDocs -> {
            return taskDocs.stream().collect(Collectors.toMap(d -> d.taskId, d -> d.taskState));
        });
    }

    @Override
    public CompletableFuture<Void> updateTaskState(String taskId, TaskState taskState) {
        logger.info("{}: State changed to {}", taskId, taskState);
        if (taskState == TaskState.COMPLETED) {
            return handleTaskComplete(taskId);
        } else {
            return getTaskDoc(taskId).thenCompose(taskDoc -> {
                taskDoc.taskState = taskState;

                if (taskState == TaskState.PARTIAL_COMPLETED) {
                    taskDoc.inputTaskIdToDataMap = new HashMap<>();
                }

                Operation updateStateOp = Operation.createPatch(host, taskIdToSelfLink(taskId))
                        .setBody(taskDoc);
                return sendOperation(host, updateStateOp, null);
            });
        }
    }

    @Override
    public ServiceHost getHost() {
        return host;
    }

    private CompletableFuture<Void> postTaskDoc(TaskDoc taskDoc) {
        //logger.info("Adding new Task: taskId={} and taskName={}", taskDoc.taskId, taskDoc.taskName);
        Operation op = Operation.createPost(host, TaskDocRepository.FACTORY_LINK)
                .setBody(taskDoc);
        return sendOperation(host, op, null);
    }

    private CompletableFuture<Void> patchTaskDoc(TaskDoc taskDoc) {
        //logger.info("Updating Task: taskId={} and taskName={}", taskDoc.taskId, taskDoc.taskName);
        Operation op = Operation.createPatch(host, taskIdToSelfLink(taskDoc.taskId))
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
            if(result.documents != null) {
                result.documents.forEach((selfLink, document) -> {
                    taskDocs.add(Utils.fromJson(document, TaskDoc.class));
                });
            }
            return taskDocs;
        });
    }

    private CompletableFuture<Void> deleteTaskDoc(String taskId) {
        Operation op = Operation.createDelete(host, taskIdToSelfLink(taskId));
        return sendOperation(host, op, null);
    }

    private CompletableFuture<Void> sendJobResult(JobResult jobResult) {
        Operation op = Operation.createPost(host, JobController.SELF_LINK + WORKER_SAVE_RESULT)
                .setBody(jobResult);
        return sendOperation(host, op, null);
    }

    private static TaskDoc taskDescriptionToTaskDoc(TaskDescription taskDescription) {
        TaskDoc taskDoc = new TaskDoc();
        taskDoc.jobId = taskDescription.getJobId();
        taskDoc.taskId = taskDescription.getTaskId();
        taskDoc.taskName = taskDescription.getTaskName();
        taskDoc.inputTaskIds = taskDescription.getInputTaskIds();
        taskDoc.outputTaskIds = taskDescription.getOutputTaskIds();
        taskDoc.inputTaskIdToDataMap = new HashMap<>();
        taskDoc.inputTaskIdToCompletionMap = new HashMap<>();
        taskDoc.taskState = TaskState.WAITING;
        taskDoc.documentSelfLink = taskDoc.taskId;
        return taskDoc;
    }

    private static TaskDescription taskDocToTaskDescription(TaskDoc taskDoc) {
        TaskDescription taskDescription = new TaskDescription(taskDoc.jobId, taskDoc.taskId, taskDoc.taskName);
        taskDescription.setInputTaskIds(taskDoc.inputTaskIds);
        taskDescription.setOutputTaskIds(taskDoc.outputTaskIds);
        return taskDescription;
    }

    private TaskContext taskDocToTaskContext(TaskDoc taskDoc) {
        TaskContext taskContext = new TaskContext();
        taskContext.setTaskManager(this);
        taskContext.setTaskDescription(taskDocToTaskDescription(taskDoc));
        taskContext.setInputTaskIdToDataMap(taskDoc.inputTaskIdToDataMap);
        taskContext.setInputTaskIdToCompletionMap(taskDoc.inputTaskIdToCompletionMap);
        return taskContext;
    }

    private static String taskIdToSelfLink(String taskId) {
        return TaskDocRepository.FACTORY_LINK + "/" + taskId;
    }

    private CompletableFuture<Void> handleTaskComplete(String taskId) {
        return getTaskDoc(taskId).thenCompose(taskDoc -> {
            taskDoc.outputTaskIds.forEach(outputTaskId -> {
                WorkerTaskData taskOutput = new WorkerTaskData();
                taskOutput.setJobId(taskDoc.jobId);
                taskOutput.setSrcTaskId(taskId);
                taskOutput.setDstTaskId(outputTaskId);
                taskOutput.setData("");
                taskOutput.setSrcTaskCompleted(true);
                sendTaskOutput(taskOutput);
            });
            return deleteTaskDoc(taskId);
        });
    }
}
