package com.vmware.krypton.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class WorkerTaskSchedule {
    private String workerId;
    private List<TaskDescription> taskDescriptions;
    private Map<String, String> taskIdToWorkerIdMap;
    private Map<String, String> workerIdToHostnameMap;
}
