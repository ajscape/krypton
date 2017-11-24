package com.vmware.krypton.service.tasks;

import com.google.common.collect.Lists;
import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.service.mappers.basic.DefaultInput;
import com.vmware.xenon.common.ServiceDocument;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class SplitterTask implements Task {

    private List<ServiceDocument> docs;

    @Override
    public void execute(TaskContext taskContext) {
        List<ServiceDocument> serviceDocs = getInput(taskContext);

        List<String> outputTaskIds = new ArrayList<>(taskContext.getTaskDescription().getOutputTaskIds());
        List<List<ServiceDocument>> partition = Lists.partition(serviceDocs, outputTaskIds.size());
        IntStream.range(0, outputTaskIds.size())
                .forEach(i -> taskContext.emitOutput(outputTaskIds.get(i), new DefaultInput(partition.get(i),
                        Arrays.asList("cost"))));
    }

    private Type getInputType() {
        Type docs = null;
        try {
            docs = SplitterTask.class.getDeclaredField("docs").getGenericType();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return docs;
    }

    private List<ServiceDocument> getInput(TaskContext taskContext) {
        List<List<ServiceDocument>> input = taskContext.getInput(getInputType());
        Optional<List<ServiceDocument>> first = input.stream().findFirst();
        if (!first.isPresent()) {
            throw new RuntimeException("No Input found from Query Task");
        }
        return first.get();
    }
}
