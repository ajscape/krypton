package com.vmware.krypton.service.master;

import com.vmware.krypton.model.TaskGraph;

public interface JobToTaskGraphTransformer {

    TaskGraph transformJobToTaskGraph(TaskGraph taskGraph);
}
