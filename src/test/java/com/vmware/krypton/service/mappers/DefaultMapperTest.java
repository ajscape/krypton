package com.vmware.krypton.service.mappers;

import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.service.mappers.basic.DefaultMapper;
import com.vmware.krypton.service.worker.TaskManager;

public class DefaultMapperTest {

    private DefaultMapper defaultMapper;

    private TaskManager taskManager;

    @Test
    public void testMapper() {
        taskManager = mock(TaskManager.class);
        defaultMapper = new DefaultMapper();
        TaskContext taskContext = new TaskContext();
        taskContext.setTaskDescription(new TaskDescription("test", "test"));
        taskContext.setTaskManager(taskManager);
        //taskContext.
    }
}
