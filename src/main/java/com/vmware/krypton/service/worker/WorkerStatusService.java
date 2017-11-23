package com.vmware.krypton.service.worker;

import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.StatelessService;
import com.vmware.xenon.services.common.NodeGroupService;

import java.util.HashMap;
import java.util.Map;

import static com.vmware.xenon.common.UriUtils.URI_PATH_CHAR;

/**
 * Created by slk on 23-Nov-17.
 */
public class WorkerStatusService extends StatelessService {
    String SELF_LINK = "/krypton/worker-status";

    public Map<String, String> getWorkerToNodeMap() {
        Operation operation = Operation.createGet(getHost(), "/core/node-groups/default?expand");
        NodeGroupService.NodeGroupState nodeGroupState = new NodeGroupService.NodeGroupState();
        nodeGroupState = operation.getBody(NodeGroupService.NodeGroupState.class);
        return parseAndGetWorkerMap(nodeGroupState);
    }

    private Map<String,String> parseAndGetWorkerMap(NodeGroupService.NodeGroupState nodeGroupState) {
        Map<String,String> workerMap = new HashMap<>();
        nodeGroupState.nodes.forEach((workerId, nodeState) -> {
            workerMap.put(workerId, nodeState.groupReference.getHost() + ":" + nodeState.groupReference.getPort());
        });
        return workerMap;
    }
}
