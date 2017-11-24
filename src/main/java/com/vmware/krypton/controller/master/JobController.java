package com.vmware.krypton.controller.master;

import com.vmware.krypton.service.master.JobManager;
import com.vmware.krypton.service.master.JobManagerImpl;
import com.vmware.xenon.ext.jee.provider.JaxRsBridgeStatelessService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.concurrent.CompletableFuture;

import static com.vmware.krypton.controller.master.JobController.SELF_LINK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
@Path(SELF_LINK)
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class JobController extends JaxRsBridgeStatelessService{

    public static final String SELF_LINK = "/krypton/job";

    @Inject
    private JobManager jobManager;

    @POST
    @Path("/map-reduce/async")
    public CompletableFuture<JobResult> executeMapReduceAsync(JobDescription jobDescription) {
        return jobManager.executeJob(jobDescription);
    }

    @POST
    @Path("/map-reduce")
    public CompletableFuture<JobResult> executeMapReduce(JobDescription jobDescription) {
        return jobManager.executeJobAndWait(jobDescription);
    }

    @POST
    @Path("/internal/save-result")
    public CompletableFuture<Void> postJobResult(JobResult jobResult) {
        return jobManager.saveJobResult(jobResult);
    }

    @GET
    @Path("/result")
    public CompletableFuture<JobResult> getJobResult(String jobId) {
        return jobManager.getJobResult(jobId);
    }
}
