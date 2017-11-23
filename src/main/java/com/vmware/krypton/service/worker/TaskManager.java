package com.vmware.krypton.service.worker;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskData;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.xenon.common.ServiceHost;

public interface TaskManager {
    CompletableFuture<Void> receiveWorkerTaskSchedule(WorkerTaskSchedule schedule);

    CompletableFuture<Void> receiveTaskInput(WorkerTaskData taskInput);

    CompletableFuture<Void> sendTaskOutput(WorkerTaskData taskOutput);

    CompletableFuture<Map<String, TaskState>> getAllTasksStates();

    CompletableFuture<Void> updateTaskState(String taskId, TaskState taskState);

    ServiceHost getHost();
}
