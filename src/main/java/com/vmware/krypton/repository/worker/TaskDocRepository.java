package com.vmware.krypton.repository.worker;

import com.vmware.krypton.document.worker.TaskDoc;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.StatefulService;
import com.vmware.xenon.common.Utils;

public class TaskDocRepository extends StatefulService {
    public static final String FACTORY_LINK = "/krypton/task";

    public TaskDocRepository() {
        super(TaskDoc.class);
    }

    @Override
    public void handlePatch(Operation patch) {
        TaskDoc taskDoc = getState(patch);
        try {
            Utils.mergeWithState(taskDoc, patch);
        } catch (Exception e) {
            e.printStackTrace();
        }
        patch.setBody(taskDoc);
        patch.complete();
    }
}
