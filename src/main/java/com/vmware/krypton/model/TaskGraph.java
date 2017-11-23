package com.vmware.krypton.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TaskGraph {

    private Map<String, TaskDescription> nodes = new ConcurrentHashMap<>();

    public void addNode(TaskDescription taskNode) {
        nodes.putIfAbsent(taskNode.getTaskId(), taskNode);
    }

    public List<TaskDescription> getNodesByName(String name) {
        return nodes.values().stream().filter(taskDescription -> taskDescription.getTaskName().equals(name))
                .collect(Collectors.toList());
    }

    public int size(){
        return nodes.size();
    }

}
