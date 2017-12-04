package com.vmware.krypton.service.tasks;

import java.util.Collections;
import java.util.List;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.xenon.common.ODataUtils;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.services.common.QueryTask;
import com.vmware.xenon.services.common.QueryTask.QuerySpecification.QueryOption;
import com.vmware.xenon.services.common.ServiceUriPaths;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class QueryTaskCreator implements Task<String, QueryTask> {

    @Override
    public void execute(TaskContext<String, QueryTask> taskContext) {
        String odataQuery = taskContext.getInput(String.class).get(0);

        Operation localQueryTaskOp = Operation.createGet(taskContext.getTaskManager().getHost(), ServiceUriPaths.ODATA_QUERIES + "?" + odataQuery);
        QueryTask localQueryTask = ODataUtils.toQuery(localQueryTaskOp, true);
        localQueryTask.querySpec.options.add(QueryOption.OWNER_SELECTION);

        taskContext.getTaskDescription().getOutputTaskIds().forEach(outputTaskId -> {
            taskContext.emitOutput(outputTaskId, localQueryTask);
        });
    }
}
