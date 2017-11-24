package com.vmware.krypton.service.tasks;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceDocument;
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
public class OdataQueryTask implements Task<String, Collection<Object>> {

    @Override
    public void execute(TaskContext<String, Collection<Object>> taskContext) {

        String odataQuery = taskContext.getInput(String.class).get(0);
        List<String> outputTaksIds = new ArrayList<String>(taskContext.getTaskDescription().getOutputTaskIds());


        Operation post = Operation.createGet(taskContext.getTaskManager().getHost(), "/core/odata-queries?" + odataQuery)
            .setCompletion((o, e) -> {
              QueryTask rsp = o.getBody(com.vmware.xenon.services.common.QueryTask.class);
                    taskContext.emitOutput(outputTaksIds.get(0), rsp.results.documents.values());
                });

        taskContext.getTaskManager().getHost().sendRequest(post);
    }
}
