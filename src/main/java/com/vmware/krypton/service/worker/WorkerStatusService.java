package com.vmware.krypton.service.worker;

import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.StatelessService;

import java.util.Map;

/**
 * Created by slk on 23-Nov-17.
 */
public class WorkerStatusService extends StatelessService {
    String SELF_LINK = "/worker-status";

    public Map<String, String> getWorkerToNodeAddrMap(){
        Operation operation = Operation.createGet(getHost(), "/core/node-groups?expand")
                .setCompletion((o, e) -> {
                    o.getBodyRaw().
                });
    }
}
