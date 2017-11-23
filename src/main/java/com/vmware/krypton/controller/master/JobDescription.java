package com.vmware.krypton.controller.master;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
@Getter@Setter
public class JobDescription {
    public String queryTask, mapper, Reducer;
}
