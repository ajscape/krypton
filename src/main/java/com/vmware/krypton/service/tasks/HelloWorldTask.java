package com.vmware.krypton.service.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;

public class HelloWorldTask implements Task<String, String> {
    public static final Logger logger = LoggerFactory.getLogger(HelloWorldTask.class);

    @Override
    public void execute(TaskContext<String, String> taskContext) {
        String text = taskContext.getInput().iterator().next();
        logger.info("Hello World");
        logger.info("Input Data = " + text);

        taskContext.getTaskDescription().getOutputTaskIds().forEach(taskId ->
            taskContext.emitOutput(taskId, text)
        );
    }
}
