package com.vmware.krypton.service.master;

import com.vmware.krypton.util.XenonUtil;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceHost;
import com.vmware.xenon.common.StatelessService;
import com.vmware.xenon.services.common.NodeGroupService;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.vmware.xenon.common.UriUtils.URI_PATH_CHAR;

/**
 * Created by slk on 23-Nov-17.
 */
public class WorkerStatusService {

    public CompletableFuture<Map<String, String>> getWorkerToNodeMap(ServiceHost host) {
        Operation operation = Operation.createGet(host, "/core/node-groups/default?expand");
        return XenonUtil.sendOperation(host, operation, NodeGroupService.NodeGroupState.class)
                .thenApply(WorkerStatusService::parseAndGetWorkerMap);
    }

    public static Map<String, String> getWorkerToHostnameMap(ServiceHost host) {
        Operation operation = Operation.createGet(host, "/core/node-groups/default?expand");
        return XenonUtil.sendOperation(host, operation, NodeGroupService.NodeGroupState.class)
                .thenApply(WorkerStatusService::parseAndGetWorkerMap).join();
    }

    private static Map<String,String> parseAndGetWorkerMap(NodeGroupService.NodeGroupState nodeGroupState) {
        Map<String, String> workerMap = new HashMap<>();
        nodeGroupState.nodes.forEach((workerId, nodeState) -> {
            workerMap.put(workerId, nodeState.groupReference.getScheme() + "://" + nodeState.groupReference.getHost() + ":" +
                    + nodeState.groupReference.getPort());
        });
        return workerMap;
    }
}
