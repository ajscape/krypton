package com.vmware.krypton.service.master;

import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.xenon.common.ServiceHost;

import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by slk on 23-Nov-17.
 */
@NoArgsConstructor
@Slf4j
public class WorkerTaskScheduleGeneratorImpl implements WorkerTaskScheduleGenerator {

    @Inject
    ServiceHost host;

    WorkerStatusService workerStatusService = new WorkerStatusService();

    public WorkerTaskScheduleGeneratorImpl(ServiceHost serviceHost){
        this.host = serviceHost;
    }

    @Override
    public CompletableFuture<List<WorkerTaskSchedule>> generateWorkerTaskSchedules(TaskGraph taskGraph) {
        log.info("Generating worker task schedules from taskGraph");
        return workerStatusService.getWorkerToNodeMap(host)
                .thenApply(workerToHostMap -> getWorkerTaskSchedules(taskGraph, workerToHostMap));
    }

    public List<WorkerTaskSchedule> getWorkerTaskSchedules(TaskGraph taskGraph, Map<String, String> workerToHostMap) {
        Map<String, TaskDescription> taskGraphMap = taskGraph.getNodes();
        List<WorkerTaskSchedule> workerTaskSchedules = new ArrayList<>();

//        workerToHostMap.forEach((k,v) -> {
//            WorkerTaskSchedule workerTaskSchedule = new WorkerTaskSchedule();
//            workerTaskSchedule.setHostname(workerToHostMap.get(k));
//            workerTaskSchedule.setTaskDescriptions(new ArrayList<>());
//            workerTaskSchedule.setTaskIdToHostnameMap(new HashMap<>());
//            workerTaskSchedules.add(workerTaskSchedule);
//        });
//
//        int i=0;
//        int taskSchedule = workerTaskSchedules.size();
//
//        for (Map.Entry<String, TaskDescription> entry : taskGraphMap.entrySet()){
//            updateTaskToWorker(workerTaskSchedules.get(i % taskSchedule), entry, workerToHostMap);
//            updateTaskIdToWorkerMap(workerTaskSchedules, entry);
//            i++;
//        }
        Map<String, String> taskIdToHostnameMap = new HashMap<>();
        taskIdToHostnameMap.put("1", "http://localhost:8000");
        taskIdToHostnameMap.put("2", "http://localhost:8000");
        taskIdToHostnameMap.put("3", "http://localhost:8000");
        taskIdToHostnameMap.put("4", "http://localhost:8001");
        taskIdToHostnameMap.put("5", "http://localhost:8000");
        taskIdToHostnameMap.put("6", "http://localhost:8001");
        taskIdToHostnameMap.put("7", "http://localhost:8001");

        Map<String, TaskDescription> nodes = taskGraph.getNodes();

        WorkerTaskSchedule schedule1 = new WorkerTaskSchedule();
        schedule1.setHostname("http://localhost:8000");
        schedule1.setTaskIdToHostnameMap(taskIdToHostnameMap);
        schedule1.setTaskDescriptions(Lists.newArrayList(nodes.get("1"), nodes.get("2"), nodes.get("3"), nodes.get("5")));

        WorkerTaskSchedule schedule2 = new WorkerTaskSchedule();
        schedule2.setHostname("http://localhost:8001");
        schedule2.setTaskIdToHostnameMap(taskIdToHostnameMap);
        schedule2.setTaskDescriptions(Lists.newArrayList(nodes.get("4"), nodes.get("6"), nodes.get("7")));

        workerTaskSchedules.add(schedule1);
        workerTaskSchedules.add(schedule2);
        return workerTaskSchedules;
    }

    private void updateTaskIdToWorkerMap(List<WorkerTaskSchedule> workerTaskSchedules, Map.Entry<String, TaskDescription> taskMap) {
        workerTaskSchedules.forEach(taskSchedule -> {
            Map<String, String> taskIdMap = taskSchedule.getTaskIdToHostnameMap();
            taskIdMap.put(taskMap.getKey(), taskSchedule.getHostname());
            taskSchedule.setTaskIdToHostnameMap(taskIdMap);
        });
    }

    private void updateTaskToWorker(WorkerTaskSchedule taskSchedule, Map.Entry<String, TaskDescription> taskMap, Map<String, String> workerToHostMap) {
        List<TaskDescription> taskDescriptionList = taskSchedule.getTaskDescriptions();
        taskDescriptionList.add(taskMap.getValue());

        Map<String, String> taskIdMap = taskSchedule.getTaskIdToHostnameMap();
        taskIdMap.put(taskMap.getKey(), workerToHostMap.get(taskSchedule.getHostname()));
        taskSchedule.setTaskIdToHostnameMap(taskIdMap);
    }

}
