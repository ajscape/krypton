package com.vmware.krypton.service.master;

import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskSchedule;

import java.util.Map;

public interface JobManager {
    void executeJob(JobDescription job);

    Map<String, TaskState> pollWorkerTaskState(String nodeId);

    void sendNodeTasksScedule(String workerId, WorkerTaskSchedule workerTaskSchedule);
}
