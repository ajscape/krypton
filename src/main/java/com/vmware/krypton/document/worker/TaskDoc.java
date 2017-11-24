package com.vmware.krypton.document.worker;

import java.util.Map;
import java.util.Set;

import com.vmware.krypton.model.TaskState;
import com.vmware.xenon.common.ServiceDocument;

public class TaskDoc extends ServiceDocument {
    public String jobId;
    public String taskId;
    public String taskName;
    public Set<String> inputTaskIds;
    public Set<String> outputTaskIds;
    public Map<String, String> inputTaskIdToDataMap;
    public Map<String, Boolean> inputTaskIdToCompletionMap;
    public TaskState taskState;
}
