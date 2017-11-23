package com.vmware.krypton.service.master;

import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskSchedule;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class JobManagerImpl implements JobManager {

    @Inject
    private JobToTaskGraphTransformer transformer;

    @Inject
    private WorkerTaskScheduleGenerator generator;

    @Override
    public void executeJob(JobDescription job) {
        TaskGraph taskGraph = transformer.transformJobToTaskGraph(job);
        CompletableFuture<List<WorkerTaskSchedule>> listCompletableFuture = generator.generateWorkerTaskSchedules(taskGraph);
    }

    @Override
    public Map<String, TaskState> pollWorkerTaskState(String nodeId) {
        return null;
    }

    @Override
    public void sendNodeTasksScedule(String workerId, WorkerTaskSchedule workerTaskSchedule) {

    }
}
