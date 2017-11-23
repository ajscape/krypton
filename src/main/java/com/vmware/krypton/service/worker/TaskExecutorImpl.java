package com.vmware.krypton.service.worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.model.TaskState;

/**
 * Created by slk on 23-Nov-17.
 */
public class TaskExecutorImpl implements TaskExecutor {

    @Override
    public <I, O> void executeTask(Task<I, O> task, TaskContext<I, O> taskContext) {

        ExecutorService executorService = Executors.newFixedThreadPool(30);

        executorService.execute(new Runnable() {
            public void run() {
                taskContext.updateTaskState(TaskState.RUNNING);
                task.execute(taskContext);
                taskContext.updateTaskState(TaskState.PARTIAL_COMPLETED);
            }
        });

        executorService.shutdown();
    }
}
