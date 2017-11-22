package com.vmware.krypton.model;

import java.util.List;
import java.util.Map;

public class NodeTasksSchedule {
    public String nodeId;
    public List<TaskDescription> taskDescriptions;
    public Map<String, String> taskIdToNodeIdMap;
}
