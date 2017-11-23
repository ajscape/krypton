package com.vmware.krypton.service.master;

import com.vmware.krypton.controller.master.JobDescription;
import com.vmware.krypton.model.TaskDescription;
import com.vmware.krypton.model.TaskGraph;

import java.util.Collections;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class JobToTaskGraphTransformerImpl implements JobToTaskGraphTransformer{
    @Override
    public TaskGraph transformJobToTaskGraph(JobDescription jobDescription) {

        return null;
    }

    private TaskDescription createQueryTask(String query){
        TaskDescription xenonQuery = new TaskDescription("1", "XenonQuery");
        return xenonQuery;
    }
}
