package com.vmware.krypton.service.tasks;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.Utils;
import com.vmware.xenon.ext.jee.query.XenonQueryService;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class QueryTask implements Task {

    @Override
    public void execute(TaskContext taskContext) {

        List<String> input = new ArrayList<String>(taskContext.getInput());
        List<String> outputTaksIds = new ArrayList<String>(taskContext.getTaskDescription().getOutputTaskIds());

        QueryTask queryTask = Utils.fromJson(input.get(0), QueryTask.class);

        Operation post = Operation.createPost(taskContext.getTaskManager().getHost(), "/core/query-tasks")
            .setBody(queryTask)
            .setCompletion((o, e) -> {
                    QueryTask rsp = o.getBody(QueryTask.class);
                    taskContext.emitOutput(outputTaksIds.get(0), rsp);
                });

        taskContext.getTaskManager().getHost().sendRequest(post);
    }
}
