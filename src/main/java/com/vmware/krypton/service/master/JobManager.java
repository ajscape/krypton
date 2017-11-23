package com.vmware.krypton.service.master;

import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskSchedule;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface JobManager {
    void executeJob(JobDescription job);

    Map<String, TaskState> pollWorkerTaskState(String nodeId);

    CompletableFuture<Object> sendNodeTasksScedule(WorkerTaskSchedule workerTaskSchedule);
}
