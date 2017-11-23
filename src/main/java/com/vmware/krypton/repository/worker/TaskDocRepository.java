package com.vmware.krypton.repository.worker;

import com.vmware.krypton.document.worker.TaskDoc;
import com.vmware.xenon.common.StatefulService;

public class TaskDocRepository extends StatefulService {
    public TaskDocRepository() {
        super(TaskDoc.class);
    }
}
