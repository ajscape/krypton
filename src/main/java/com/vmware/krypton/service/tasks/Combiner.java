package com.vmware.krypton.service.tasks;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class Combiner implements Task<Map<String, List<Object>>, Map<String, List<Object>>> {

    private Collection<Map<String, Object>> inputType;

    @Override
    public void execute(TaskContext taskContext) {
        List<Map<String, Object>> inputs = getInput(taskContext);

        Map<String, Object> output = new HashMap<>();
        inputs.stream().forEach(stringObjectMap -> output.putAll(stringObjectMap));

        taskContext.emitOutput("0", output);
    }

    public void executeTemp(TaskContext taskContext) {
        List<Map<String, Object>> inputs = getInput(taskContext);

        Map<String, Object> output = new HashMap<>();
        inputs.stream().forEach(stringObjectMap -> output.putAll(stringObjectMap));

        List<String> inputTaskIds = taskContext.getTaskDescription().getInputTaskIds().stream()
                .filter(s -> !s.equals(taskContext.getTaskDescription().getTaskId())).collect(Collectors.toList());

        long noOfCompletedInputTask = inputTaskIds.stream()
                .filter(inputTaskId -> taskContext.getInputTaskIdToCompletionMap().get(inputTaskId).equals(Boolean.TRUE)).count();
        if (noOfCompletedInputTask == inputTaskIds.size()) {
            taskContext.emitOutput("0", output);
        } else {
            taskContext.emitOutput(taskContext.getTaskDescription().getTaskId(), output);
        }
    }

    private List<Map<String, Object>> getInput(TaskContext taskContext) {
        Collection<Map<String, List<Object>>> input = null;
        try {
            input = taskContext
                    .getInput(Combiner.class.getDeclaredField("inputType").getGenericType());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return new ArrayList(input);
    }
}
