package com.vmware.krypton.model;

import java.util.Collection;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import com.vmware.krypton.service.worker.TaskManager;

@Getter
@Setter
public class TaskContext<I,O> {

    private TaskDescription taskDescription;
    private Map<String, I> inputTaskIdToDataMap;
    private TaskManager taskManager;

    public Collection<I> getInput() {
        return inputTaskIdToDataMap.values();
    }

    public void emitOutput(String outputTaskId, O output) {
        WorkerTaskData taskOutput = new WorkerTaskData();
        taskOutput.setSrcTaskId(taskDescription.getTaskId());
        taskOutput.setDstTaskId(outputTaskId);
        taskOutput.setData(output);
        taskManager.sendTaskOutput(taskOutput).join();
    }

    public void updateTaskState(TaskState taskState) {
        taskManager.updateTaskState(taskDescription.getTaskId(), taskState).join();
    }
}
