package com.vmware.krypton.service.master;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.junit.Test;

import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.model.WorkerTaskSchedule;
import com.vmware.krypton.service.master.WorkerTaskScheduleGeneratorImpl;
import com.vmware.xenon.common.ServiceHost;

/**
 * Created by slk on 23-Nov-17.
 */
public class WorkerTaskSchedulerTest {

    ServiceHost host;
    WorkerTaskScheduleGeneratorImpl workerTaskScheduleGenerator = new WorkerTaskScheduleGeneratorImpl(host);

//    @Test
    public void testTaskSchedule(){
        List<WorkerTaskSchedule> task = workerTaskScheduleGenerator.getWorkerTaskSchedules(getTaskGraph(), getWorkerToHostMap());
    }

    private TaskGraph getTaskGraph() {
        TaskGraph taskGraph = new TaskGraph();
        taskGraph.addNode(new TaskDescription("J1", "t1", "a"));
        taskGraph.addNode(new TaskDescription("J1", "t2", "b"));
        taskGraph.addNode(new TaskDescription("J1", "t3", "c"));
        taskGraph.addNode(new TaskDescription("J1", "t4", "d"));
        taskGraph.addNode(new TaskDescription("J1", "t5", "e"));
        return taskGraph;
    }

    private Map<String, String> getWorkerToHostMap() {
        Map<String, String> worker = new HashMap<>();
        worker.put("w1", "10.112.1.1");
        worker.put("w2", "10.112.1.2");
        worker.put("w3", "10.112.1.3");
        return worker;
    }
}
