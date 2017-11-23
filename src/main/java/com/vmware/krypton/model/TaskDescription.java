package com.vmware.krypton.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class TaskDescription {
    private final String taskId;
    private final String taskName;
    private Set<String> inputTaskIds = new HashSet<>();
    private Set<String> outputTaskIds = new HashSet<>();

    public void addInputTaskId(String taskId){
        inputTaskIds.add(taskId);
    }

    public void addOutputTaskIds(String... taskIds){
        outputTaskIds.addAll(Arrays.asList(taskIds));
    }
}
