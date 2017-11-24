package com.vmware.krypton.service.tasks;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;

import java.util.*;

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
