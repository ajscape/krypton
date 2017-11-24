package com.vmware.krypton.controller.worker;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import static com.vmware.krypton.controller.worker.WorkerTaskController.SELF_LINK;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskData;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.krypton.service.worker.TaskManager;
import com.vmware.xenon.ext.jee.annotations.OperationBody;
import com.vmware.xenon.ext.jee.provider.JaxRsBridgeStatelessService;

@Path(SELF_LINK)
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class WorkerTaskController extends JaxRsBridgeStatelessService {

    public static final String SELF_LINK = "/krypton/worker";
    public static final String TASK_SCHEDULE = "/task-schedule";
    public static final String TASK_INPUT = "/task-input";

    @Inject
    private TaskManager taskManager;

    @POST
    @Path(TASK_SCHEDULE)
    public CompletableFuture<Void> postWorkerTaskSchedule(@OperationBody WorkerTaskSchedule schedule) {
        return taskManager.receiveWorkerTaskSchedule(schedule);
    }

    @POST
    @Path(TASK_INPUT)
    public CompletableFuture<Void> postWorkerTaskInput(@OperationBody WorkerTaskData taskInput) {
        return taskManager.receiveTaskInput(taskInput);
    }

    @GET
    @Path("/task-states")
    public CompletableFuture<Map<String, TaskState>> getAllTasksStates() {
        return taskManager.getAllTasksStates();
    }
}
