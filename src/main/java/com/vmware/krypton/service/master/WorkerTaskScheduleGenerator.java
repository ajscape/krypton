package com.vmware.krypton.service.master;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.model.WorkerTaskSchedule;

public interface WorkerTaskScheduleGenerator {
     CompletableFuture<List<WorkerTaskSchedule>> generateWorkerTaskSchedules(TaskGraph taskGraph);
}
