package com.vmware.krypton.service.mappers;

import com.vmware.xenon.common.ServiceDocument;
import java.util.List;
import java.util.Map;

/**
 * Created by sivarajm on 11/23/2017.
 */
public interface Mapper <M extends MapContext>{

  void map(List<? extends ServiceDocument> documents, M mapContext) throws MapperException;
}
