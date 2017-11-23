package com.vmware.krypton.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
        addOutputTaskIds(Arrays.asList(taskIds));
    }

    public void addOutputTaskIds(Collection<String> taskIds){
        outputTaskIds.addAll(taskIds);
    }

}
