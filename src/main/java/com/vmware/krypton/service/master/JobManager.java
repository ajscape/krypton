package com.vmware.krypton.service.master;

import java.util.Map;

import com.vmware.krypton.model.Job;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.krypton.model.TaskState;

public interface JobManager {
    void executeJob(Job job);

    Map<String, TaskState> pollWorkerTaskState(String nodeId);

    void sendNodeTasksScedule(String workerId, WorkerTaskSchedule workerTaskSchedule);
}
