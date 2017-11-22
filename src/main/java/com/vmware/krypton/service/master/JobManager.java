package com.vmware.krypton.service.master;

import java.util.Map;

import com.vmware.krypton.model.Job;
import com.vmware.krypton.model.NodeTasksSchedule;
import com.vmware.krypton.model.TaskState;

public interface JobManager {
    void executeJob(Job job);

    Map<String, TaskState> pollNodeTasksState(String nodeId);

    void sendNodeTasksScedule(String nodeId, NodeTasksSchedule nodeTasksSchedule);
}
