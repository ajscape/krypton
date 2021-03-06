package com.vmware.krypton.host;

import java.util.logging.Level;

import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.StatefulService;

public class TestService extends StatefulService {

    public static final String FACTORY_LINK = "/test";

    public static class TestState extends ServiceDocument {
        public Integer cost;
    }

    public TestService() {
        super(TestState.class);
        toggleOption(ServiceOption.PERSISTENCE, true);
        toggleOption(ServiceOption.REPLICATION, true);
        toggleOption(ServiceOption.OWNER_SELECTION, true);
    }

    @Override
    public void handleStart(Operation post) {
        getHost().log(Level.INFO, "Received post");
        super.handleStart(post);
    }
}
