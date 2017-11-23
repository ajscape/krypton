package com.vmware.krypton.model;

import java.util.List;
import java.util.Map;

public class WorkerTaskSchedule {
    public String workerId;
    public List<TaskDescription> taskDescriptions;
    public Map<String, String> taskIdToWorkerIdMap;
}
