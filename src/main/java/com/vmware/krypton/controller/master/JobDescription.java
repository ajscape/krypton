package com.vmware.krypton.controller.master;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
@Getter@Setter@ToString
public class JobDescription {
    public String queryTask, mapper, Reducer;
}
