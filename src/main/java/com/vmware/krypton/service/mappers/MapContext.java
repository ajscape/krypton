package com.vmware.krypton.service.mappers;

import java.util.List;
import java.util.Map;

/**
 * Created by sivarajm on 11/23/2017.
 */
public abstract class MapContext < O extends MapOutput>{

  public abstract void emmit(O output);
}
