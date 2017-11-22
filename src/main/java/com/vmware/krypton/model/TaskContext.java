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
        taskManager.sendTaskOutput(taskDescription.getTaskId(), outputTaskId, output);
    }

    public void updateTaskState(TaskState taskState) {
        taskManager.updateTaskState(taskDescription.getTaskId(), taskState);
        if(taskState == TaskState.COMPLETED) {
            taskDescription.getOutputTaskIds().forEach(outputTaskId ->
                    taskManager.sendTaskCompletionEvent(taskDescription.getTaskId(), outputTaskId));
        }
    }
}
