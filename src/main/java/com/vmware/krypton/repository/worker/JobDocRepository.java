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
}
