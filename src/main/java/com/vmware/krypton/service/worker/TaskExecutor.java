package com.vmware.krypton.service.worker;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;

public interface TaskExecutor {

    <I,O> void executeTask(Task<I, O> task, TaskContext<I, O> taskContext);
}
