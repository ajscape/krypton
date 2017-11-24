package com.vmware.krypton.service.tasks;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.Utils;
import com.vmware.xenon.ext.jee.query.XenonQueryService;
import com.vmware.xenon.services.common.QueryTask;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class InputQueryTask implements Task {

    @Override
    public void execute(TaskContext taskContext) {

        List<QueryTask> input = taskContext.getInput(QueryTask.class);
        List<String> outputTaksIds = new ArrayList<String>(taskContext.getTaskDescription().getOutputTaskIds());

        QueryTask queryTask = input.get(0);

        Operation post = Operation.createPost(taskContext.getTaskManager().getHost(), "/core/query-tasks")
            .setBody(queryTask)
            .setCompletion((o, e) -> {
              com.vmware.xenon.services.common.QueryTask rsp = o.getBody(com.vmware.xenon.services.common.QueryTask.class);
                    taskContext.emitOutput(outputTaksIds.get(0), rsp.results.documents.values());
                });

        taskContext.getTaskManager().getHost().sendRequest(post);
    }
}
