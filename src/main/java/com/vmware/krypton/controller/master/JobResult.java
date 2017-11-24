package com.vmware.krypton.controller.master;

import lombok.Data;

@Data
public class JobResult {
    private String jobId;
    private String result;
    private boolean success;
    private String errorMessage;
}
