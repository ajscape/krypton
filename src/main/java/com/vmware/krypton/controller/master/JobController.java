package com.vmware.krypton.controller.master;

import com.vmware.krypton.service.master.JobManager;
import com.vmware.krypton.service.master.JobManagerImpl;
import com.vmware.xenon.ext.jee.provider.JaxRsBridgeStatelessService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
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

    public static final String SELF_LINK = "/krypton/mapreduce";

    @Inject
    private JobManager jobManager;

    @POST
    @Path("/map_reduce")
    public CompletableFuture<Object> executeMapReduce(JobDescription jobDescription) {
        jobManager.executeJob(jobDescription);
        return CompletableFuture.completedFuture("Job Successfully Submitted");
    }

}
