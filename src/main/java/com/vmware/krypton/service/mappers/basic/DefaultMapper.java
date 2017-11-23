package com.vmware.krypton.service.mappers.basic;

import com.vmware.krypton.service.mappers.Mapper;
import com.vmware.krypton.service.mappers.MapperException;
import com.vmware.xenon.common.ServiceDocument;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sivarajm on 11/23/2017.
 */
public class DefaultMapper implements Mapper<DefaultContext> {

  @Override
  public void map(List<? extends ServiceDocument> documents, DefaultContext mapContext) throws MapperException {

    DefaultOutput defaultOutput = new DefaultOutput();
    Map<String, List<Object>> resultMap = new HashMap<>();
    defaultOutput.setOutputMap(resultMap);

    for (ServiceDocument document : documents) {
      for (String fieldName : mapContext.getExtractDataMembers()) {
        try {
          Class aClass = document.getClass();
          Field field = aClass.getField(fieldName);
          Object value = field.get(document);

          resultMap.computeIfPresent(fieldName, (k,v) -> { v.add(value); return v;});
          resultMap.computeIfAbsent(fieldName, (k) -> Arrays.asList(value));
        } catch (Exception e) {
          throw new MapperException(e.getMessage(), e.getCause());
        }
      }
    }

    mapContext.emmit(defaultOutput);

  }
}
