package com.vmware.krypton.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkerTaskData {
    private Object data;
    private String srcTaskId;
    private String dstTaskId;
    boolean isSrcTaskCompletionEvent;
}
