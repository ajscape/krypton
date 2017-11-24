package com.vmware.krypton.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import com.vmware.krypton.service.worker.TaskManager;
import com.vmware.xenon.common.Utils;

@Getter
@Setter
public class TaskContext<I, O> {

    private TaskDescription taskDescription;
    private Map<String, String> inputTaskIdToDataMap;
    private Map<String, Boolean> inputTaskIdToCompletionMap;
    private TaskManager taskManager;

    public List<I> getInput(Class<I> type) {
        return inputTaskIdToDataMap.entrySet().stream()
                .filter((entry -> inputTaskIdToCompletionMap.get(entry.getKey()) == null))
                .map(entry -> Utils.fromJson(entry.getValue(), type))
                .collect(Collectors.toList());
    }

    public void emitOutput(String outputTaskId, O output) {
        WorkerTaskData taskOutput = new WorkerTaskData();
        taskOutput.setSrcTaskId(taskDescription.getTaskId());
        taskOutput.setDstTaskId(outputTaskId);
        taskOutput.setData(Utils.toJson(output));
        taskManager.sendTaskOutput(taskOutput).join();
    }

    public void updateTaskState(TaskState taskState) {
        taskManager.updateTaskState(taskDescription.getTaskId(), taskState).join();
    }
}
