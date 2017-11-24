package com.vmware.krypton.service.tasks;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class Combiner implements Task<Map<String, List<Object>>, Map<String, List<Object>>> {

    private Collection<Map<String, List<Object>>> inputType;

    @Override
    public void execute(TaskContext taskContext) {

        Collection<Map<String, List<Object>>> input = null;
        try {
            input = taskContext
                    .getInput(Combiner.class.getDeclaredField("inputType").getGenericType());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        List<String> outputTaksIds = new ArrayList<String>(taskContext.getTaskDescription().getOutputTaskIds());

        List<Map<String, List<Object>>> maps = new ArrayList<>(input);

        Map<String, List<Object>> output = new HashMap<>();

        maps.forEach( item -> {
            item.forEach((k,v)->{
                output.computeIfPresent(k, (k1, v1) -> {
                    v1.addAll(v);
                    return v1;
                });

                output.computeIfAbsent(k, k1 -> v);
            });
        });

        taskContext.emitOutput(outputTaksIds.get(0), output);
    }
}
