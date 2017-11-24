package com.vmware.krypton.service.master;

import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.controller.master.JobResult;
import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskSchedule;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface JobManager {
    CompletableFuture<JobResult> executeJob(JobDescription job);

    CompletableFuture<JobResult> executeJobAndWait(JobDescription job);

    Map<String, TaskState> pollWorkerTaskState(String nodeId);

    CompletableFuture<Object> sendNodeTasksSchedule(WorkerTaskSchedule workerTaskSchedule);

    CompletableFuture<Void> saveJobResult(JobResult jobResult);

    CompletableFuture<JobResult> getJobResult(String jobId);
}
