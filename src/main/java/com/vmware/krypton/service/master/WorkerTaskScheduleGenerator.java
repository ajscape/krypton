package com.vmware.krypton.service.master;

import java.util.List;

import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.model.WorkerTaskSchedule;

public interface WorkerTaskScheduleGenerator {

     List<WorkerTaskSchedule> generateWorkerTaskSchedules(TaskGraph taskGraph);
}
