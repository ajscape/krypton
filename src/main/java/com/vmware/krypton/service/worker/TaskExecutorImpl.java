package com.vmware.krypton.service.worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.model.TaskState;

/**
 * Created by slk on 23-Nov-17.
 */
@Slf4j
public class TaskExecutorImpl implements TaskExecutor {

    @Override
    public <I, O> void executeTask(Task<I, O> task, TaskContext<I, O> taskContext) {

        ExecutorService executorService = Executors.newFixedThreadPool(30);

        executorService.execute(new Runnable() {
            public void run() {
                taskContext.updateTaskState(TaskState.RUNNING);
                TaskDescription taskDescription = taskContext.getTaskDescription();
                log.info("Task {}: Executing '{}'", taskDescription.getTaskId(), taskDescription.getTaskName());
                try {
                    task.execute(taskContext);
                } catch (Exception e) {
                    throw new RuntimeException("Error in Task " + taskDescription.getTaskId() + ":", e);
                }
                taskContext.updateTaskState(TaskState.PARTIAL_COMPLETED);
            }
        });

        executorService.shutdown();
    }
}
