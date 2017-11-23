package com.vmware.krypton.service.worker;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.vmware.krypton.model.TaskState;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.krypton.repository.worker.TaskDocRepository;

public class TaskManagerImpl implements TaskManager {

    private TaskDocRepository taskRepository;

    public TaskManagerImpl(TaskDocRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public CompletableFuture<Void> receiveWorkerTaskSchedule(WorkerTaskSchedule schedule) {
        return null;
    }

    @Override
    public CompletableFuture<Void> receiveTaskInput(String srcTaskId, String dstTaskId, Object input) {
        return null;
    }

    @Override
    public CompletableFuture<Void> receiveTaskCompletionEvent(String srcTaskId, String dstTaskId) {
        return null;
    }

    @Override
    public CompletableFuture<Void> sendTaskOutput(String srcTaskId, String dstTaskId, Object Output) {
        return null;
    }

    @Override
    public CompletableFuture<Void> sendTaskCompletionEvent(String srcTaskId, String dstTaskId) {
        return null;
    }

    @Override
    public CompletableFuture<Map<String, TaskState>> getAllTasksStates() {
        return null;
    }

    @Override
    public CompletableFuture<Void> updateTaskState(String taskId, TaskState taskState) {
        return null;
    }
}
