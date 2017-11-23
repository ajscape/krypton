package com.vmware.krypton.service.master;

import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.model.TaskGraph;
import com.vmware.krypton.service.tasks.Combiner;
import com.vmware.krypton.service.tasks.QueryTask;
import com.vmware.krypton.service.tasks.ReducerTask;
import com.vmware.krypton.service.tasks.SplitterTask;

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
        dag.addNode(new TaskDescription("2", SplitterTask.class.getName()));
        IntStream.rangeClosed(3, getNumberOfNodes() + 2)
                .forEach(taskIdInt -> dag.addNode(new TaskDescription(Objects.toString(taskIdInt), "Mapper")));
        IntStream.rangeClosed(dag.size() + 1, getNumberOfNodes() + dag.size())
                .forEach(taskIdInt -> dag.addNode(new TaskDescription(Objects.toString(taskIdInt), ReducerTask.class.getName())));
        dag.addNode(new TaskDescription(Objects.toString(dag.size() + 1), Combiner.class.getName()));
        return dag;
    }

    private TaskDescription createQueryTask(String query) {
        TaskDescription xenonQuery = new TaskDescription("1", "XenonQuery");
        return xenonQuery;
    }
}
