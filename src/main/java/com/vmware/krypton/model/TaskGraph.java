package com.vmware.krypton.model;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class TaskGraph {

    private Map<String, TaskDescription> nodes = new ConcurrentHashMap<>();

    public void addNode(TaskDescription taskNode) {
        nodes.putIfAbsent(taskNode.getTaskId(), taskNode);
    }

    public List<TaskDescription> getNodesByName(String name) {
        return nodes.values().stream().filter(taskDescription -> taskDescription.getTaskName().equals(name))
                .collect(Collectors.toList());
    }

    public void addEdge(String sourceNodeId, List<TaskDescription> destNodes) {
        TaskDescription sourceTask = nodes.get(sourceNodeId);
        sourceTask.addOutputTaskIds(destNodes.stream().map(TaskDescription::getTaskId).collect(Collectors.toList()));

        destNodes.forEach(destTaskDes -> destTaskDes.addInputTaskId(sourceNodeId));
    }

    public int size() {
        return nodes.size();
    }

}
