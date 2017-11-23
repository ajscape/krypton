package com.vmware.krypton.service.master;

import com.spotify.futures.CompletableFutures;
import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceHost;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.vmware.krypton.controller.worker.WorkerTaskController.SELF_LINK;
import static com.vmware.krypton.controller.worker.WorkerTaskController.TASK_SCHEDULE;
import static com.vmware.krypton.util.XenonUtil.sendOperation;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class JobManagerImpl implements JobManager {

    @Inject
    private JobToTaskGraphTransformer transformer;

    @Inject
    private WorkerTaskScheduleGenerator generator;

    @Inject
    private ServiceHost host;

    @Override
    public void executeJob(JobDescription job) {
        TaskGraph taskGraph = transformer.transformJobToTaskGraph(job);
        CompletableFuture<List<WorkerTaskSchedule>> workerSchedulesCF = generator.generateWorkerTaskSchedules(taskGraph);
        workerSchedulesCF.thenCompose(workerTaskSchedules -> {
            List<CompletableFuture<Object>> scheduledTaskCFs = workerTaskSchedules.stream().map(this::sendNodeTasksScedule)
                    .collect(Collectors.toList());
            return CompletableFutures.allAsList(scheduledTaskCFs);
        });
    }

    @Override
    public Map<String, TaskState> pollWorkerTaskState(String nodeId) {
        return null;
    }

    @Override
    public CompletableFuture<Object> sendNodeTasksScedule(WorkerTaskSchedule workerTaskSchedule) {
        String workerNodeId = workerTaskSchedule.getWorkerIdToHostnameMap().get(workerTaskSchedule.getWorkerId());
        String workerNodeURL = workerNodeId + SELF_LINK + TASK_SCHEDULE;
        Operation op = Operation.createPost(host, workerNodeURL)
                .setBody(workerTaskSchedule);
        return sendOperation(host, op, null);

    }
}
