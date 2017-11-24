package com.vmware.krypton.host;

import com.vmware.krypton.controller.master.JobController;
import com.vmware.krypton.controller.worker.WorkerTaskController;
import com.vmware.krypton.repository.worker.JobDocRepository;
import com.vmware.krypton.repository.worker.TaskDocRepository;
import com.vmware.xenon.common.ServiceHost;
import com.vmware.xenon.ext.jee.consumer.JaxRsServiceClient;
import com.vmware.xenon.ext.jee.host.InjectableHost;

public class KryptonHost {

    public static void main(String[] args) throws Throwable {
        //        KryptonHost h = new KryptonHost();
        //        h.initialize(args);
        //        h.start();
        //        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        //            h.log(Level.WARNING, "Host stopping ...");
        //            h.stop();
        //            h.log(Level.WARNING, "Host is stopped");
        //        }));

        InjectableHost.HostBuilder hostBuilder = InjectableHost.newBuilder();
        hostBuilder.withArguments(args).withArguments(new ServiceHost.Arguments());
        hostBuilder.withCdiModule(new KryptonModule());
        hostBuilder.withStatelessService(new JobController()).asPrivilegedService(JobController.class);
        hostBuilder.withStatelessService(new WorkerTaskController());

        hostBuilder.withStatefulService(new TaskDocRepository());
        hostBuilder.withStatefulService(new JobDocRepository());
        hostBuilder.withStatefulService(new TestService());

        InjectableHost injectableHost = hostBuilder.buildAndStart();

        injectableHost.getHost().getClient().stop();
        injectableHost.getHost().getClient().setSSLContext(JaxRsServiceClient.createAcceptAllSslContext());
        injectableHost.getHost().getClient().start();
    }

    //    @Override
    //    public ServiceHost start() throws Throwable {
    //        super.start();
    //        startDefaultCoreServicesSynchronously();
    //
    //        // Start the root namespace factory: this will respond to the root URI (/) and list all
    //        // the factory services.
    //        super.startService(new RootNamespaceService());
    //        super.startFactory(new TestService());
    //
    //        startKryptonWorkerServices();
    //        startKryptonMasterServices();
    //
    //        return this;
    //    }
    //
    //    public void startKryptonWorkerServices() {
    //        TaskExecutor taskExecutor = new TaskExecutorImpl();
    //        TaskManager taskManager = new TaskManagerImpl(this, taskExecutor);
    //
    //        super.startService(new WorkerTaskController(taskManager));
    //        super.startFactory(new TaskDocRepository());
    //    }
    //
    //    public void startKryptonMasterServices() {
    ////        super.startService(new WorkerStatusService());
    //    }
}
