package com.vmware.krypton.service.master;

import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.service.mappers.basic.MapperTask;
import com.vmware.krypton.service.tasks.Combiner;
import com.vmware.krypton.service.tasks.DocumentReader;
import com.vmware.krypton.service.tasks.OdataQueryTask;
import com.vmware.krypton.service.tasks.QueryTaskCreator;
import com.vmware.krypton.service.tasks.ReducerTask;
import com.vmware.krypton.service.tasks.SplitterTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static com.vmware.krypton.util.XenonUtil.getNumberOfNodes;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
@Slf4j
public class JobToTaskGraphTransformerImpl implements JobToTaskGraphTransformer {
    public static final String MASTER_TASK_ID = "MASTER";

    @Override
    public TaskGraph transformJobToTaskGraph(JobDescription jobDescription) {
        log.info("Creating task graph for job {}", jobDescription.getJobId());
        return createMapReduceTaskGraph2(jobDescription);
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
                .forEach(taskIdInt -> dag.addNode(new TaskDescription(jobId,Objects.toString(taskIdInt), MapperTask.class.getName())));
        List<TaskDescription> mapperTasks = dag.getNodesByName(MapperTask.class.getName());
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

    public TaskGraph createMapReduceTaskGraph2(JobDescription jobDescription) {
        String jobId = jobDescription.getJobId();
        TaskGraph dag = new TaskGraph();
        TaskDescription queryTask = new TaskDescription(jobId, "1", QueryTaskCreator.class.getName());
        queryTask.addInputTaskId(MASTER_TASK_ID);
        dag.addNode(queryTask);

        IntStream.rangeClosed(2, getNumberOfNodes() + 1)
                .forEach(taskIdInt -> dag.addNode(new TaskDescription(jobId, Objects.toString(taskIdInt), DocumentReader.class.getName())));
        List<TaskDescription> docReaderTasks = dag.getNodesByName(DocumentReader.class.getName());
        dag.addEdge("1", docReaderTasks);

        IntStream.rangeClosed(4, getNumberOfNodes() + 3)
                .forEach(taskIdInt -> {
                    TaskDescription mapper = new TaskDescription(jobId,Objects.toString(taskIdInt), MapperTask.class.getName());
                    dag.addNode(mapper);
                    dag.addEdge(Integer.toString(taskIdInt - getNumberOfNodes()), Collections.singletonList(mapper));
                });
        List<TaskDescription> mapperTasks = dag.getNodesByName(MapperTask.class.getName());

        IntStream.rangeClosed(dag.size() + 1, getNumberOfNodes() + dag.size())
                .forEach(taskIdInt -> dag.addNode(new TaskDescription(jobId, Objects.toString(taskIdInt), ReducerTask.class.getName())));
        List<TaskDescription> reducerTasks = dag.getNodesByName(ReducerTask.class.getName());
        mapperTasks.forEach(mapperTask -> dag.addEdge(mapperTask.getTaskId(), reducerTasks));

        TaskDescription combinerTask = new TaskDescription(jobId, Objects.toString(dag.size() + 1), Combiner.class.getName());
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
