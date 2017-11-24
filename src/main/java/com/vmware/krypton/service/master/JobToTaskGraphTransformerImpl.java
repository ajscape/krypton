package com.vmware.krypton.service.master;

import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.service.mappers.basic.DefaultMapper;
import com.vmware.krypton.service.tasks.Combiner;
import com.vmware.krypton.service.tasks.OdataQueryTask;
import com.vmware.krypton.service.tasks.ReducerTask;
import com.vmware.krypton.service.tasks.SplitterTask;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static com.vmware.krypton.util.XenonUtil.getNumberOfNodes;

import com.google.common.collect.Sets;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class JobToTaskGraphTransformerImpl implements JobToTaskGraphTransformer {
    public static final String MASTER_TASK_ID = "MASTER";

    @Override
    public TaskGraph transformJobToTaskGraph(JobDescription jobDescription) {
        return createMapReduceTaskGraph(jobDescription);
//        return createHelloWorldTaskGraph(jobDescription);
    }

    public TaskGraph createMapReduceTaskGraph(JobDescription jobDescription) {
        String jobId = jobDescription.getJobId();
        TaskGraph dag = new TaskGraph();
        TaskDescription queryTask = new TaskDescription(jobId, "1", OdataQueryTask.class.getName());
        queryTask.addInputTaskId(MASTER_TASK_ID);
        dag.addNode(queryTask);

        TaskDescription splitterTask = new TaskDescription(jobId,"2", SplitterTask.class.getName());
        dag.addNode(splitterTask);
        dag.addEdge("1", Arrays.asList(splitterTask));

        IntStream.rangeClosed(3, getNumberOfNodes() + 2)
                .forEach(taskIdInt -> dag.addNode(new TaskDescription(jobId,Objects.toString(taskIdInt), DefaultMapper.class.getName())));
        List<TaskDescription> mapperTasks = dag.getNodesByName(DefaultMapper.class.getName());
        dag.addEdge("2", mapperTasks);

        IntStream.rangeClosed(dag.size() + 1, getNumberOfNodes() + dag.size())
                .forEach(taskIdInt -> dag.addNode(new TaskDescription(jobId,Objects.toString(taskIdInt), ReducerTask.class.getName())));
        List<TaskDescription> reducerTasks = dag.getNodesByName(ReducerTask.class.getName());
        mapperTasks.forEach(mapperTask -> dag.addEdge(mapperTask.getTaskId(), reducerTasks));

        TaskDescription combinerTask = new TaskDescription(jobId,Objects.toString(dag.size() + 1), Combiner.class.getName());
        dag.addNode(combinerTask);
        reducerTasks.forEach(reducerTask -> dag.addEdge(reducerTask.getTaskId(), Arrays.asList(combinerTask)));

        combinerTask.addOutputTaskIds(MASTER_TASK_ID);
        dag.setInputTaskId("1");
        return dag;
    }

    public TaskGraph createHelloWorldTaskGraph(JobDescription jobDescription) {
        TaskDescription t1 = new TaskDescription(jobDescription.getJobId(), "T1", "helloWorld", Sets.newHashSet(MASTER_TASK_ID), Sets.newHashSet("T2"));
        TaskDescription t2 = new TaskDescription(jobDescription.getJobId(), "T2", "helloWorld", Sets.newHashSet("T1"), Sets.newHashSet(MASTER_TASK_ID));

        TaskGraph taskGraph = new TaskGraph();
        taskGraph.addNode(t1);
        taskGraph.addNode(t2);
        taskGraph.setInputTaskId("T1");
        return taskGraph;
    }
}
