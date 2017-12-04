package com.vmware.krypton.service.tasks;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.vmware.krypton.host.TestService.TestState;
import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.service.mappers.basic.DefaultInput;
import com.vmware.krypton.util.XenonUtil;
import com.vmware.xenon.common.ODataUtils;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.QueryResultsProcessor;
import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.ServiceHost;
import com.vmware.xenon.common.Utils;
import com.vmware.xenon.services.common.QueryTask;
import com.vmware.xenon.services.common.QueryTask.QuerySpecification.QueryOption;
import com.vmware.xenon.services.common.ServiceUriPaths;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class DocumentReader implements Task<QueryTask, DefaultInput> {

    @Override
    public void execute(TaskContext<QueryTask, DefaultInput> taskContext) {
        QueryTask localQueryTask = taskContext.getInput(QueryTask.class).get(0);
        ServiceHost host = taskContext.getTaskManager().getHost();
        Operation op = Operation.createPost(host, ServiceUriPaths.CORE_LOCAL_QUERY_TASKS)
                .setBody(localQueryTask);

        QueryTask result = XenonUtil.sendOperation(host, op, QueryTask.class).join();
        List<TestState> documents;
        if(result.results.documents != null) {
            documents = result.results.documents.entrySet().stream()
                    .map(e -> Utils.fromJson(e.getValue(), TestState.class))
                    .collect(Collectors.toList());
        } else {
            documents = new ArrayList<>();
        }

        taskContext.getTaskDescription().getOutputTaskIds().forEach(outputTaskId -> {
            taskContext.emitOutput(outputTaskId, new DefaultInput(documents, Collections.singletonList("cost")));
        });
    }
}
