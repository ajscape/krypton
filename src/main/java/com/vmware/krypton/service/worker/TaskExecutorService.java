package com.vmware.krypton.service.worker;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.model.TaskState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by slk on 23-Nov-17.
 */
public class TaskExecutorService implements TaskExecutor {

    @Override
    public <I, O> void executeTask(Task<I, O> task, TaskContext<I, O> taskContext) {

        ExecutorService executorService = Executors.newFixedThreadPool(30);

        executorService.execute(new Runnable() {
            public void run() {
                taskContext.updateTaskState(TaskState.RUNNING);
                task.execute(taskContext);
                taskContext.updateTaskState(TaskState.PAUSED);
            }
        });

        executorService.shutdown();
    }
}
