package com.vmware.krypton.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDescription {
    private String taskId;
    private String taskName;
    private List<String> inputTaskIds;
    private List<String> outputTaskIds;
}
