package com.vmware.krypton.controller.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobResult {
    public String jobId;
    public boolean isCompleted;
    public boolean success;
    public String result;
    private String errorMessage;
}
