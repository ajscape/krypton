package com.vmware.krypton.document.worker;

import com.vmware.xenon.common.ServiceDocument;

public class JobDoc extends ServiceDocument {
    public String jobId;
    public boolean isCompleted;
    public boolean success;
    public String result;
    private String errorMessage;
}
