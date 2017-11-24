package com.vmware.krypton.service.mappers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vmware.krypton.service.mappers.basic.DefaultInput;
import com.vmware.xenon.common.ServiceDocument;
import org.junit.Assert;
import org.junit.Test;

import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.service.mappers.basic.DefaultMapper;
import com.vmware.krypton.service.worker.TaskManager;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class DefaultMapperTest {

    private DefaultMapper defaultMapper;

    private TaskManager taskManager;

    @Mock
    TaskContext taskContext;

    @Test
    public void testMapper() {
        taskManager = mock(TaskManager.class);
        defaultMapper = new DefaultMapper();
        Mockito.when(taskContext.getInput(any())).thenReturn(getMapperInput());
        Mockito.when(taskContext.getTaskDescription()).thenReturn(getTaskDescription());
        defaultMapper.execute(taskContext);
        ArgumentCaptor<TaskContext> argumentCaptor = ArgumentCaptor.forClass(TaskContext.class);
        doNothing().when(taskContext).emitOutput(any(), any());
        verify(taskContext, times(2)).emitOutput(Mockito.anyString(),argumentCaptor.capture());
        List<TaskContext> taskContexts = argumentCaptor.getAllValues();
        Assert.assertTrue(taskContexts.size()==2);
    }


    private TaskDescription getTaskDescription() {
        TaskDescription taskDescription = new TaskDescription("1", "2", "name");
        Set<String> outputTaskIds = new HashSet<>();
        outputTaskIds.add("t1");
        outputTaskIds.add("t2");
        taskDescription.setOutputTaskIds(outputTaskIds);
        return taskDescription;
    }

    private List<DefaultInput> getMapperInput() {
        List<DefaultInput> defaultInputs = new ArrayList<>();
        DefaultInput defaultInput = new DefaultInput();
        defaultInput.setDocuments(getDocuments());
        defaultInput.setExtractDataMembers(Arrays.asList("a","b"));
        defaultInputs.add(defaultInput);
        return defaultInputs;
    }
    public class TestState1 extends ServiceDocument {
        public String a;
        public String b;

        public TestState1(String a, String b){
            this.a = a;
            this.b = b;
        }
    }


    private List<? extends ServiceDocument> getDocuments() {
        List<TestState1> serviceDocuments = new ArrayList<>();
        serviceDocuments.add(new TestState1("1", "2"));
        serviceDocuments.add(new TestState1("3", "4"));
        return serviceDocuments;
    }
}
