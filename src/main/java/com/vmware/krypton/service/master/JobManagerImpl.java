package com.vmware.krypton.service.master;

import com.spotify.futures.CompletableFutures;
import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.controller.master.JobResult;
import com.vmware.krypton.controller.worker.WorkerTaskController;
import com.vmware.krypton.document.worker.JobDoc;
import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskData;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.krypton.repository.worker.JobDocRepository;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceHost;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.vmware.krypton.controller.worker.WorkerTaskController.SELF_LINK;
import static com.vmware.krypton.controller.worker.WorkerTaskController.TASK_INPUT;
import static com.vmware.krypton.controller.worker.WorkerTaskController.TASK_SCHEDULE;
import static com.vmware.krypton.service.master.JobToTaskGraphTransformerImpl.MASTER_TASK_ID;
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

    private static Map<String, CompletableFuture<JobResult>> jobIdToResultFutureMap = new HashMap<>();
    private static Map<String, String> jobIdToInputTaskIdMap = new HashMap<>();
    private static Collection<String> hostnames = new ArrayList<>();

    @Override
    public CompletableFuture<JobResult> executeJob(JobDescription job) {
        String jobId = (job.getJobId() != null) ? job.getJobId() : UUID.randomUUID().toString();
        log.info("Received job {} with id={}", job, jobId);
        TaskGraph taskGraph = transformer.transformJobToTaskGraph(job);
        String inputTaskId = taskGraph.getInputTaskId();
        jobIdToInputTaskIdMap.put(jobId, inputTaskId);
        //log.info("Task Graph[{}] has been created for {}", taskGraph, job);
        CompletableFuture<List<WorkerTaskSchedule>> workerSchedulesCF = generator.generateWorkerTaskSchedules(taskGraph);
        hostnames = WorkerStatusService.getWorkerToHostnameMap(host).values();
        return workerSchedulesCF.thenCompose(workerTaskSchedules -> {
            //log.info("Got the worker schedules {}", workerTaskSchedules);
            List<CompletableFuture<Object>> scheduledTaskCFs = workerTaskSchedules.stream().map(this::sendNodeTasksSchedule)
                    .collect(Collectors.toList());
            return CompletableFutures.allAsList(scheduledTaskCFs);
        }).thenCompose(aVoid -> {
            JobDoc jobDoc = createJobDoc(jobId);
            Operation op = Operation.createPost(host, JobDocRepository.FACTORY_LINK).setBody(jobDoc);
            return sendOperation(host, op, JobResult.class);
        }).thenCompose(jobResult ->
            sendTaskInput(jobId, job.inputData, false).thenApply(aVoid -> jobResult)
        );
    }

    private CompletableFuture<Void> sendTaskInput(String jobId, String inputData, boolean isCompleted) {
        WorkerTaskData taskData = new WorkerTaskData();
        taskData.setJobId(jobId);
        taskData.setSrcTaskId(MASTER_TASK_ID);
        taskData.setDstTaskId(jobIdToInputTaskIdMap.get(jobId));
        taskData.setData(inputData);
        taskData.setSrcTaskCompleted(isCompleted);
        hostnames.forEach(hostname -> {
            URI uri = URI.create(hostname + WorkerTaskController.SELF_LINK + TASK_INPUT);
            Operation op1 = Operation.createPost(uri).setBody(taskData).addPragmaDirective(Operation.PRAGMA_DIRECTIVE_NO_FORWARDING);
            sendOperation(host, op1, null);
        });
        return CompletableFuture.completedFuture(null);
    }


    @Override
    public CompletableFuture<JobResult> executeJobAndWait(JobDescription job) {
        CompletableFuture<JobResult> jobResultFuture = new CompletableFuture<>();
        executeJob(job).thenAccept(jobResult -> {
            jobIdToResultFutureMap.put(jobResult.getJobId(), jobResultFuture);
            //log.info("Adding future");
        });
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
        log.info("Sending task schedule to Worker-Node:{}", workerNodeURL);
        return sendOperation(host, op, null);
    }

    @Override
    public CompletableFuture<Void> saveJobResult(JobResult jobResult) {
        Operation op = Operation.createPatch(host, JobDocRepository.FACTORY_LINK + "/" + jobResult.getJobId())
                .setBody(jobResult);
        log.info("Job {}: Received result = {}", jobResult.getJobId(), jobResult.getResult());
        return sendOperation(host, op, null).thenCompose(aVoid -> {
            CompletableFuture<JobResult> future = jobIdToResultFutureMap.get(jobResult.getJobId());
            if(future != null) {
                future.complete(jobResult);
            }
//            deleteJobTasks(jobResult.getJobId()).thenCompose(aVoid1 -> {
//                Operation delJob = Operation.createDelete(host, JobDocRepository.FACTORY_LINK + "/" + jobResult.getJobId());
//                return sendOperation(host, delJob, null);
//            });
            return CompletableFuture.completedFuture(null);
        });
    }

    private CompletableFuture<Void> deleteJobTasks(String jobId) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        hostnames.forEach(hostname -> {
            URI uri = URI.create(hostname + WorkerTaskController.SELF_LINK + "/job/" + jobId);
            Operation delJob = Operation.createDelete(uri);
            futures.add(sendOperation(host, delJob, null));
        });
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).exceptionally(e -> { throw new RuntimeException(e); });
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
