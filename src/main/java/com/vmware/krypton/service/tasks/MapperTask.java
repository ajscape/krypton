package com.vmware.krypton.service.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;

public abstract class MapperTask <K1, V1, K2 extends Object, V2> implements Task<Map<K1, V1>, Map<K2, V2>> {

    private TaskContext<Map<K1,V1>, Map<K2,V2>> taskContext;
    private Map<K2, V2> outputData = new HashMap<>();

    public abstract void map(K1 key, V1 value);

    @Override
    public void execute(TaskContext<Map<K1, V1>, Map<K2, V2>> taskContext) {
        this.taskContext = taskContext;
        Map<K1, V1> input = taskContext.getInput().iterator().next();
        input.forEach((key, value) -> {
            map(key, value);
        });
    }

    public void emit(K2 key, V2 value) {
        outputData.put(key, value);
    }

    public int getOutputPartitionNumber(K2 key, int maxPartitions) {
        return key.hashCode() % maxPartitions;
    }

    private Map<String, Map<K2, V2>> getOuputPartitions() {
        Map<String, Map<K2, V2>> outTaskIdToDataMap = new HashMap<>();

        Map<Integer, String> partitionNumberToDstTaskMap = new HashMap<>();
        Set<String> outputTaskIds = taskContext.getTaskDescription().getOutputTaskIds();
        int i = 0;
        for(String outputTaskId : outputTaskIds) {
            partitionNumberToDstTaskMap.put(i, outputTaskId);
            outTaskIdToDataMap.put(outputTaskId, new HashMap<>());
            i++;
        }
        outputData.forEach((k, v) -> {
            int partitionNum = getOutputPartitionNumber(k, outputTaskIds.size());
            outTaskIdToDataMap.get(partitionNumberToDstTaskMap.get(partitionNum)).put(k, v);
        });
        return outTaskIdToDataMap;
    }
}
