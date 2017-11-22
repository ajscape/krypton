package com.vmware.krypton.host;

import java.util.logging.Level;

import com.vmware.xenon.common.ServiceHost;
import com.vmware.xenon.services.common.RootNamespaceService;

public class KryptonHost extends ServiceHost {

    public static void main(String[] args) throws Throwable {
        KryptonHost h = new KryptonHost();
        h.initialize(args);
        h.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            h.log(Level.WARNING, "Host stopping ...");
            h.stop();
            h.log(Level.WARNING, "Host is stopped");
        }));
    }

    @Override
    public ServiceHost start() throws Throwable {
        super.start();
        startDefaultCoreServicesSynchronously();

        // Start the root namespace factory: this will respond to the root URI (/) and list all
        // the factory services.
        super.startService(new RootNamespaceService());
        super.startFactory(new TestService());

        return this;
    }
}
