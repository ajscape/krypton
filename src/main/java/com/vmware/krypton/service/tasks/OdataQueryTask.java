package com.vmware.krypton.service.tasks;

import com.vmware.krypton.host.TestService.TestState;
import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.util.XenonUtil;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.ServiceHost;
import com.vmware.xenon.common.Utils;
import com.vmware.xenon.ext.jee.query.XenonQueryService;
import com.vmware.xenon.services.common.QueryTask;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class OdataQueryTask implements Task<String, List<TestState>> {

    @Override
    public void execute(TaskContext<String, List<TestState>> taskContext) {

        String odataQuery = taskContext.getInput(String.class).get(0);
        List<String> outputTaksIds = new ArrayList<String>(taskContext.getTaskDescription().getOutputTaskIds());

        ServiceHost host = taskContext.getTaskManager().getHost();
        Operation op = Operation.createGet(taskContext.getTaskManager().getHost(), "/core/odata-queries?" + odataQuery);
        QueryTask rsp = XenonUtil.sendOperation(host, op, QueryTask.class).join();
        taskContext.emitOutput(outputTaksIds.get(0), rsp.results.documents.values().stream()
                .map(d -> Utils.fromJson(d, TestState.class)).collect(Collectors.toList()));
    }
}
