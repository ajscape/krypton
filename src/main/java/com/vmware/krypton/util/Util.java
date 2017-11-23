package com.vmware.krypton.util;

import java.util.concurrent.CompletableFuture;

import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceHost;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class Util {

    public int getNumberOfNodes() {
        //TODO Implement....
        return 3;
    }

    public <R> CompletableFuture<R> sendOperation(ServiceHost host, Operation op, Class<? extends R> resultClass) {
        CompletableFuture<R> future = new CompletableFuture<>();
        op.setCompletion((o, e) -> {
            if (e != null) {
                future.completeExceptionally(e);
            }
            if (resultClass.equals(Void.class)) {
                future.complete(null);
            } else {
                R result = o.getBody(resultClass);
                future.complete(result);
            }
        }).sendWith(host);
        return future;
    }
}
