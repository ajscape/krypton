package com.vmware.krypton.repository.worker;

import com.vmware.krypton.document.worker.JobDoc;
import com.vmware.krypton.document.worker.TaskDoc;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.StatefulService;

public class JobDocRepository extends StatefulService {
    public static final String FACTORY_LINK = "/krypton/master/data/job";

    public JobDocRepository() {
        super(JobDoc.class);
        toggleOption(ServiceOption.REPLICATION, true);
        toggleOption(ServiceOption.OWNER_SELECTION, true);
    }

    @Override
    public void handlePatch(Operation patch) {
        JobDoc jobDoc = patch.getBody(JobDoc.class);
        JobDoc currentState = getState(patch);
        currentState.success = jobDoc.success;
        currentState.result = jobDoc.result;
        currentState.isCompleted = jobDoc.isCompleted;
        currentState.errorMessage = jobDoc.errorMessage;
        setState(patch, currentState);
        patch.setBody(currentState);
        patch.complete();
    }
}
