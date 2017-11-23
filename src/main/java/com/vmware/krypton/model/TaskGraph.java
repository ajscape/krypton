package com.vmware.krypton.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskGraph {

    private Map<String, TaskDescription> nodes = new ConcurrentHashMap<>();

    public void addNode(TaskDescription taskNode){
        nodes.putIfAbsent(taskNode.getTaskId(), taskNode);
    }





}
