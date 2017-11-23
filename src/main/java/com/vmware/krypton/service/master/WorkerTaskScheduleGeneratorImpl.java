package com.vmware.krypton.service.master;

import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.krypton.service.worker.WorkerStatusService;
import com.vmware.xenon.common.ServiceHost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Created by slk on 23-Nov-17.
 */
public class WorkerTaskScheduleGeneratorImpl implements WorkerTaskScheduleGenerator {

    ServiceHost host;

    WorkerStatusService workerStatusService = new WorkerStatusService();

    public WorkerTaskScheduleGeneratorImpl(ServiceHost serviceHost){
        this.host = serviceHost;
    }

    @Override
    public CompletableFuture<List<WorkerTaskSchedule>> generateWorkerTaskSchedules(TaskGraph taskGraph) {
        return workerStatusService.getWorkerToNodeMap(host)
                .thenApply(workerToHostMap -> getWorkerTaskSchedules(taskGraph, workerToHostMap));
    }

    public List<WorkerTaskSchedule> getWorkerTaskSchedules(TaskGraph taskGraph, Map<String, String> workerToHostMap) {
        Map<String, TaskDescription> taskGraphMap = taskGraph.getNodes();
        List<WorkerTaskSchedule> workerTaskSchedules = new ArrayList<>();

        workerToHostMap.forEach((k,v) -> {
            WorkerTaskSchedule workerTaskSchedule = new WorkerTaskSchedule();
            workerTaskSchedule.setWorkerId(k);
            workerTaskSchedule.setWorkerIdToHostnameMap(workerToHostMap);
            workerTaskSchedule.setTaskDescriptions(new ArrayList<>());
            workerTaskSchedule.setTaskIdToWorkerIdMap(new HashMap<>());
            workerTaskSchedules.add(workerTaskSchedule);
        });

        int i=0;
        int taskSchedule = workerTaskSchedules.size();

        for (Map.Entry<String, TaskDescription> entry : taskGraphMap.entrySet()){
            updateTaskToWorker(workerTaskSchedules.get(i % taskSchedule), entry);
            updateTaskIdToWorkerMap(workerTaskSchedules, entry);
            i++;
        }
        return workerTaskSchedules;
    }

    private void updateTaskIdToWorkerMap(List<WorkerTaskSchedule> workerTaskSchedules, Map.Entry<String, TaskDescription> taskMap) {
        workerTaskSchedules.forEach(taskSchedule -> {
            Map<String, String> taskIdMap = taskSchedule.getTaskIdToWorkerIdMap();
            taskIdMap.put(taskMap.getKey(), taskSchedule.getWorkerId());
            taskSchedule.setTaskIdToWorkerIdMap(taskIdMap);
        });
    }

    private void updateTaskToWorker(WorkerTaskSchedule taskSchedule, Map.Entry<String, TaskDescription> taskMap) {
        List<TaskDescription> taskDescriptionList = taskSchedule.getTaskDescriptions();
        taskDescriptionList.add(taskMap.getValue());
        taskSchedule.setTaskDescriptions(taskDescriptionList);
        Map<String, String> taskIdMap = taskSchedule.getTaskIdToWorkerIdMap();
        taskIdMap.put(taskMap.getKey(), taskSchedule.getWorkerId());
        taskSchedule.setTaskIdToWorkerIdMap(taskIdMap);
    }

}
