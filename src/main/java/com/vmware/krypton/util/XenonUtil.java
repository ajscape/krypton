package com.vmware.krypton.util;

import java.util.concurrent.CompletableFuture;

import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceHost;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class XenonUtil {

    public static int getNumberOfNodes() {
        //TODO Implement....
        return 3;
    }

    public static <R> CompletableFuture<R> sendOperation(ServiceHost host, Operation op, Class<? extends R> resultClass) {
        CompletableFuture<R> future = new CompletableFuture<>();
        op.setReferer(host.getUri());
        op.setCompletion((o, e) -> {
            if (e != null) {
                future.completeExceptionally(e);
            }
            if (resultClass == null) {
                future.complete(null);
            } else {
                R result = o.getBody(resultClass);
                future.complete(result);
            }
        }).sendWith(host);
        return future;
    }
}
