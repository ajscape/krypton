package com.vmware.krypton.service.master;

import com.spotify.futures.CompletableFutures;
import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.controller.master.JobResult;
import com.vmware.krypton.document.worker.JobDoc;
import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.krypton.repository.worker.JobDocRepository;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceHost;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    private Map<String, CompletableFuture<JobResult>> jobIdToResultFutureMap = new HashMap<>();

    @Override
    public CompletableFuture<JobResult> executeJob(JobDescription job) {
        String jobId = UUID.randomUUID().toString();
        log.info("Received job {} with id={}", job, jobId);
        log.info("Going to create a DAG for {}", job);
        TaskGraph taskGraph = transformer.transformJobToTaskGraph(job);
        log.info("Task Graph[{}] has been created for {}", taskGraph, job);
        CompletableFuture<List<WorkerTaskSchedule>> workerSchedulesCF = generator.generateWorkerTaskSchedules(taskGraph);
        return workerSchedulesCF.thenCompose(workerTaskSchedules -> {
            log.info("Got the worker schedules {}", workerTaskSchedules);
            List<CompletableFuture<Object>> scheduledTaskCFs = workerTaskSchedules.stream().map(this::sendNodeTasksSchedule)
                    .collect(Collectors.toList());
            return CompletableFutures.allAsList(scheduledTaskCFs);
        }).thenCompose(aVoid -> {
            JobDoc jobDoc = createJobDoc(jobId);
            Operation op = Operation.createPost(host, JobDocRepository.FACTORY_LINK).setBody(jobDoc);
            return sendOperation(host, op, JobResult.class);
        });
    }

    @Override
    public CompletableFuture<JobResult> executeJobAndWait(JobDescription job) {
        CompletableFuture<JobResult> jobResultFuture = new CompletableFuture<>();
        executeJob(job).thenAccept(jobResult -> jobIdToResultFutureMap.put(jobResult.getJobId(), jobResultFuture));
        return jobResultFuture;
    }

    @Override
    public Map<String, TaskState> pollWorkerTaskState(String nodeId) {
        return null;
    }

    @Override
    public CompletableFuture<Object> sendNodeTasksSchedule(WorkerTaskSchedule workerTaskSchedule) {
        URI workerNodeURL = URI.create(workerTaskSchedule.getHostname() + SELF_LINK + TASK_SCHEDULE);
        Operation op = Operation.createPost(workerNodeURL)
                .setBody(workerTaskSchedule);
        log.info("Sending the task[{}] to Worker-Node:{}", workerTaskSchedule.getTaskDescriptions(), workerNodeURL);
        return sendOperation(host, op, null);
    }

    @Override
    public CompletableFuture<Void> saveJobResult(JobResult jobResult) {
        Operation op = Operation.createPatch(host, JobDocRepository.FACTORY_LINK + "/" + jobResult.getJobId())
                .setBody(jobResult);
        return sendOperation(host, op, null).thenAccept(aVoid -> {
            CompletableFuture<JobResult> future = jobIdToResultFutureMap.get(jobResult.getJobId());
            if(future != null) {
                future.complete(jobResult);
            }
        });
    }

    @Override
    public CompletableFuture<JobResult> getJobResult(String jobId) {
        Operation op = Operation.createGet(host, JobDocRepository.FACTORY_LINK + "/" + jobId);
        return sendOperation(host, op, JobResult.class);
    }

    private JobDoc createJobDoc(String jobId) {
        JobDoc jobDoc = new JobDoc();
        jobDoc.jobId = jobId;
        jobDoc.isCompleted = false;
        jobDoc.documentSelfLink = jobId;
        return jobDoc;
    }
}
