package com.vmware.krypton.service.master;

import com.spotify.futures.CompletableFutures;
import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceHost;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class JobManagerImpl implements JobManager {

    @Inject
    private JobToTaskGraphTransformer transformer;

    @Inject
    private WorkerTaskScheduleGenerator generator;

    @Inject
    private ServiceHost host;

    @Override
    public void executeJob(JobDescription job) {
        log.info("Going to create a DAG for {}", job);
        TaskGraph taskGraph = transformer.transformJobToTaskGraph(job);
        log.info("Task Graph[{}] has been created for {}", taskGraph, job);
        CompletableFuture<List<WorkerTaskSchedule>> workerSchedulesCF = generator.generateWorkerTaskSchedules(taskGraph);
        workerSchedulesCF.thenCompose(workerTaskSchedules -> {
            log.info("Got the worker schedules {}", workerTaskSchedules);
            List<CompletableFuture<Object>> scheduledTaskCFs = workerTaskSchedules.stream().map(this::sendNodeTasksSchedule)
                    .collect(Collectors.toList());
            return CompletableFutures.allAsList(scheduledTaskCFs);
        });
    }

    @Override
    public Map<String, TaskState> pollWorkerTaskState(String nodeId) {
        return null;
    }

    @Override
    public CompletableFuture<Object> sendNodeTasksSchedule(WorkerTaskSchedule workerTaskSchedule) {
        String workerNodeId = workerTaskSchedule.getWorkerIdToHostnameMap().get(workerTaskSchedule.getWorkerId());
        String workerNodeURL = workerNodeId + SELF_LINK + TASK_SCHEDULE;
        Operation op = Operation.createPost(host, workerNodeURL)
                .setBody(workerTaskSchedule);
        log.info("Sending the task[{}] to Worker-Node:{}", workerTaskSchedule.getTaskDescriptions(), workerNodeURL);
        return sendOperation(host, op, null);
    }
}
