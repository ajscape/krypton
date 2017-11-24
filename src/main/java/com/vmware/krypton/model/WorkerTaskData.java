package com.vmware.krypton.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkerTaskData {
    private String jobId;
    private String data;
    private String srcTaskId;
    private String dstTaskId;
    boolean isSrcTaskCompleted;
}
