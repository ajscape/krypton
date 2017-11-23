package com.vmware.krypton.controller.worker;

import java.util.concurrent.CompletableFuture;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.vmware.krypton.model.WorkerTaskData;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.krypton.service.worker.TaskManager;
import com.vmware.xenon.ext.jee.annotations.OperationBody;
import com.vmware.xenon.ext.jee.provider.JaxRsBridgeStatelessService;

public class WorkerTaskController extends JaxRsBridgeStatelessService {

    public static final String SELF_lINK = "/krypton/worker";

    private TaskManager taskManager;

    public WorkerTaskController(TaskManager taskManager) {
        super();
        this.taskManager = taskManager;
    }

    @POST
    @Path("/task-schedule")
    CompletableFuture<Void> postWorkerTaskSchedule(@OperationBody WorkerTaskSchedule schedule) {
        return taskManager.receiveWorkerTaskSchedule(schedule);
    }

    @POST
    @Path("/task-input")
    CompletableFuture<Void> postWorkerTaskInput(@OperationBody WorkerTaskData taskInput) {
        return taskManager.receiveTaskInput(taskInput);
    }

}
