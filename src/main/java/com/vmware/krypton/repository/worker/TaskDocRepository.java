package com.vmware.krypton.repository.worker;

import com.vmware.krypton.document.worker.TaskDoc;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.StatefulService;

public class TaskDocRepository extends StatefulService {
    public static final String FACTORY_LINK = "/krypton/task";

    public TaskDocRepository() {
        super(TaskDoc.class);
//        toggleOption(ServiceOption.REPLICATION, true);
//        toggleOption(ServiceOption.OWNER_SELECTION, true);
    }

    @Override
    public void handlePatch(Operation patch) {
        TaskDoc taskDoc = patch.getBody(TaskDoc.class);
        TaskDoc currentState = getState(patch);
        if(taskDoc.taskState != null) {
            currentState.taskState = taskDoc.taskState;
        }
        if(taskDoc.inputTaskIdToDataMap != null) {
            currentState.inputTaskIdToDataMap = taskDoc.inputTaskIdToDataMap;
        }
        if(taskDoc.inputTaskIdToCompletionMap != null) {
            currentState.inputTaskIdToCompletionMap = taskDoc.inputTaskIdToCompletionMap;
        }
        setState(patch, currentState);
        patch.setBody(currentState);
        patch.complete();
    }
}
