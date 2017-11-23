package com.vmware.krypton.repository.worker;

import com.vmware.krypton.document.worker.TaskDoc;
import com.vmware.xenon.common.StatefulService;

public class TaskDocRepository extends StatefulService {
    public static final String FACTORY_LINK = "/krypton/task";

    public TaskDocRepository() {
        super(TaskDoc.class);
    }
}
