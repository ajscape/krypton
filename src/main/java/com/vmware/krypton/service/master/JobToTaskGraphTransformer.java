package com.vmware.krypton.service.master;

import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.model.TaskGraph;

public interface JobToTaskGraphTransformer {

    TaskGraph transformJobToTaskGraph(JobDescription jobDescription);
}
