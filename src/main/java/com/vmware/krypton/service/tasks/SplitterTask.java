package com.vmware.krypton.service.tasks;

import com.google.common.collect.Lists;

import com.vmware.krypton.host.TestService.TestState;
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
public class SplitterTask implements Task<List<TestState>, DefaultInput> {

    private List<TestState> docs;

    @Override
    public void execute(TaskContext<List<TestState>, DefaultInput> taskContext) {
        List<TestState> serviceDocs = getInput(taskContext);

        List<String> outputTaskIds = new ArrayList<>(taskContext.getTaskDescription().getOutputTaskIds());
        List<List<TestState>> partition = Lists.partition(serviceDocs, outputTaskIds.size());
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

    private List<TestState> getInput(TaskContext taskContext) {
        List<List<TestState>> input = taskContext.getInput(getInputType());
        Optional<List<TestState>> first = input.stream().findFirst();
        if (!first.isPresent()) {
            throw new RuntimeException("No Input found from Query Task");
        }
        return first.get();
    }
}
