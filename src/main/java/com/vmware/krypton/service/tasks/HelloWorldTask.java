package com.vmware.krypton.service.tasks;

import java.util.List;

import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.service.tasks.HelloWorldTask.Message;

public class HelloWorldTask implements Task<Message, Message> {
    public static final Logger logger = LoggerFactory.getLogger(HelloWorldTask.class);

    @Override
    public void execute(TaskContext<Message, Message> taskContext) {
        List<Message> messages = taskContext.getInput(Message.class);
        logger.info("{}: Executing task with data = {}", taskContext.getTaskDescription().getTaskId(), messages);
        taskContext.getTaskDescription().getOutputTaskIds().forEach(taskId ->
            messages.forEach(message -> taskContext.emitOutput(taskId, message))
        );
    }

    @ToString
    public static class Message {
        public String key;
        public String value;
    }
}
