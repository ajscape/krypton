package com.vmware.krypton.service.worker;

import java.util.Map;

import com.vmware.krypton.model.NodeTasksSchedule;
import com.vmware.krypton.model.TaskState;

public interface TaskManager {
    void receiveNodeTasksSchedule(NodeTasksSchedule schedule);

    void receiveTaskInput(String srcTaskId, String dstTaskId, Object input);

    void receiveTaskCompletionEvent(String srcTaskId, String dstTaskId);

    void sendTaskOutput(String srcTaskId, String dstTaskId, Object Output);

    void sendTaskCompletionEvent(String srcTaskId, String dstTaskId);

    Map<String, TaskState> getAllTasksStates();

    void updateTaskState(String taskId, TaskState taskState);
}
