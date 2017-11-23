package com.vmware.krypton.controller.worker;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.vmware.krypton.model.WorkerTaskData;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.krypton.service.worker.TaskManager;
import com.vmware.xenon.ext.jee.annotations.OperationBody;
import com.vmware.xenon.ext.jee.provider.JaxRsBridgeStatelessService;

public class WorkerTaskController extends JaxRsBridgeStatelessService {

    public static final String SELF_LINK = "/krypton/worker";
    public static final String TASK_SCHEDULE = "/task-schedule";

    @Inject
    private TaskManager taskManager;

    @POST
    @Path(TASK_SCHEDULE)
    CompletableFuture<Void> postWorkerTaskSchedule(@OperationBody WorkerTaskSchedule schedule) {
        return taskManager.receiveWorkerTaskSchedule(schedule);
    }

    @POST
    @Path("/task-input")
    CompletableFuture<Void> postWorkerTaskInput(@OperationBody WorkerTaskData taskInput) {
        return taskManager.receiveTaskInput(taskInput);
    }

}
