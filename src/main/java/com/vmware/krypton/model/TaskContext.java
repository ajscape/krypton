package com.vmware.krypton.model;

import com.vmware.krypton.service.worker.TaskManager;
import com.vmware.xenon.common.Utils;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class TaskContext<I, O> {

    private TaskDescription taskDescription;
    private Map<String, String> inputTaskIdToDataMap;
    private Map<String, Boolean> inputTaskIdToCompletionMap;
    private TaskManager taskManager;

    public <T> List<T> getInput(Type type) {
        return (List<T>) inputTaskIdToDataMap.entrySet().stream()
                .filter(entry -> inputTaskIdToCompletionMap.get(entry.getKey()) == null)
                .map(entry -> Utils.fromJson(entry.getValue(), type))
                .collect(Collectors.toList());
    }

    public void emitOutput(String outputTaskId, O output) {
        WorkerTaskData taskOutput = new WorkerTaskData();
        taskOutput.setJobId(taskDescription.getJobId());
        taskOutput.setSrcTaskId(taskDescription.getTaskId());
        taskOutput.setDstTaskId(outputTaskId);
        taskOutput.setData(Utils.toJson(output));
        taskManager.sendTaskOutput(taskOutput).join();
    }

    public void updateTaskState(TaskState taskState) {
        taskManager.updateTaskState(taskDescription.getTaskId(), taskState).join();
    }
}
