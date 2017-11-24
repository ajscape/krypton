package com.vmware.krypton.service.mappers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vmware.krypton.host.TestService.TestState;
import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.service.mappers.basic.DefaultInput;
import com.vmware.krypton.service.mappers.basic.MapperTask;
import com.vmware.krypton.service.worker.TaskManager;
import com.vmware.xenon.common.ServiceDocument;

@RunWith(MockitoJUnitRunner.class)
public class MapperTaskTest {

    private MapperTask mapperTask;

    private TaskManager taskManager;

    @Mock
    TaskContext taskContext;

    @Test
    public void testMapper() {
        taskManager = mock(TaskManager.class);
        mapperTask = new MapperTask();
        Mockito.when(taskContext.getInput(any())).thenReturn(getMapperInput());
        Mockito.when(taskContext.getTaskDescription()).thenReturn(getTaskDescription());
        mapperTask.execute(taskContext);
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


    private List<TestState> getDocuments() {
        List<TestState> serviceDocuments = new ArrayList<>();
        serviceDocuments.add(new TestState());
        serviceDocuments.add(new TestState());
        return serviceDocuments;
    }
}
