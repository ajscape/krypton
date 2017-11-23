package com.vmware.krypton.service.master;

import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.service.mappers.basic.DefaultMapper;
import com.vmware.krypton.service.tasks.Combiner;
import com.vmware.krypton.service.tasks.QueryTask;
import com.vmware.krypton.service.tasks.ReducerTask;
import com.vmware.krypton.service.tasks.SplitterTask;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static com.vmware.krypton.util.XenonUtil.getNumberOfNodes;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class JobToTaskGraphTransformerImpl implements JobToTaskGraphTransformer {

    @Override
    public TaskGraph transformJobToTaskGraph(JobDescription jobDescription) {
        TaskGraph dag = new TaskGraph();
        dag.addNode(new TaskDescription("1", QueryTask.class.getName()));

        TaskDescription splitterTask = new TaskDescription("2", SplitterTask.class.getName());
        dag.addNode(splitterTask);
        dag.addEdge("1", Arrays.asList(splitterTask));

        IntStream.rangeClosed(3, getNumberOfNodes() + 2)
                .forEach(taskIdInt -> dag.addNode(new TaskDescription(Objects.toString(taskIdInt), DefaultMapper.class.getName())));
        List<TaskDescription> mapperTasks = dag.getNodesByName(DefaultMapper.class.getName());
        dag.addEdge("2", mapperTasks);

        IntStream.rangeClosed(dag.size() + 1, getNumberOfNodes() + dag.size())
                .forEach(taskIdInt -> dag.addNode(new TaskDescription(Objects.toString(taskIdInt), ReducerTask.class.getName())));
        List<TaskDescription> reducerTasks = dag.getNodesByName(ReducerTask.class.getSimpleName());
        mapperTasks.forEach(mapperTask -> dag.addEdge(mapperTask.getTaskId(), reducerTasks));

        TaskDescription combinerTask = new TaskDescription(Objects.toString(dag.size() + 1), Combiner.class.getName());
        dag.addNode(combinerTask);
        reducerTasks.forEach(reducerTask -> dag.addEdge(reducerTask.getTaskId(), Arrays.asList(combinerTask)));

        return dag;
    }

}
