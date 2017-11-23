package com.vmware.krypton.host;

import com.google.inject.AbstractModule;
import com.vmware.krypton.service.master.*;
import com.vmware.krypton.service.worker.TaskExecutor;
import com.vmware.krypton.service.worker.TaskExecutorImpl;
import com.vmware.krypton.service.worker.TaskManager;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class KryptonModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JobManager.class).to(JobManagerImpl.class);
        bind(JobToTaskGraphTransformer.class).to(JobToTaskGraphTransformerImpl.class);
        bind(TaskExecutor.class).to(TaskExecutorImpl.class);
        bind(TaskManager.class).to(TaskManager.class);
        bind(WorkerTaskScheduleGenerator.class).to(WorkerTaskScheduleGeneratorImpl.class);
    }
}

