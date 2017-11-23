package com.vmware.krypton.service.tasks;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;

public class HelloWorldTask implements Task<String, String> {
    @Override
    public void execute(TaskContext<String, String> taskContext) {
        String text = taskContext.getInput().iterator().next();
        System.out.println("Input Data: " + text);
        System.out.println("Hello World");

        taskContext.getTaskDescription().getOutputTaskIds().forEach(taskId ->
            taskContext.emitOutput(taskId, text)
        );
    }
}
