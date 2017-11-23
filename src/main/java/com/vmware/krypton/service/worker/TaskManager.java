package com.vmware.krypton.service.worker;

import com.vmware.xenon.common.ServiceHost;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskData;
import com.vmware.krypton.model.WorkerTaskSchedule;

public interface TaskManager {
    CompletableFuture<Void> receiveWorkerTaskSchedule(WorkerTaskSchedule schedule);

    CompletableFuture<Void> receiveTaskInput(WorkerTaskData taskInput);

    CompletableFuture<Void> sendTaskOutput(WorkerTaskData taskOutput);

    CompletableFuture<Void> sendTaskCompletionEvent(String srcTaskId, String dstTaskId);

    CompletableFuture<Map<String, TaskState>> getAllTasksStates();

    CompletableFuture<Void> updateTaskState(String taskId, TaskState taskState);

    ServiceHost getHost();
}
