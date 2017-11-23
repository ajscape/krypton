package com.vmware.krypton.document.worker;

import java.util.List;
import java.util.Map;

import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.TaskState;

public class TaskDoc extends ServiceDocument {
    public String taskId;
    public String taskName;
    public List<String> inputTaskIds;
    public List<String> outputTaskIds;
    public Map<String, Object> inputTaskIdToDataMap;
    public TaskState taskState;
}
