package com.vmware.krypton.service.mappers.basic;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;
import com.vmware.krypton.service.mappers.Mapper;
import com.vmware.krypton.service.mappers.MapperException;
import com.vmware.xenon.common.ServiceDocument;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sivarajm on 11/23/2017.
 */
public class DefaultMapper implements Mapper<DefaultInput>, Task<DefaultInput, Map<String, List<Object>>>  {

  @Override
  public Object map(DefaultInput input) throws MapperException {

    Map<String, List<Object>> resultMap = new HashMap<>();

    for (ServiceDocument document : input.documents) {
      for (String fieldName : input.getExtractDataMembers()) {
        try {
          Class aClass = document.getClass();
          Field field = aClass.getField(fieldName);
          Object value = field.get(document);

          resultMap.computeIfPresent(fieldName, (k,v) -> { v.add(value); return v;});
          resultMap.computeIfAbsent(fieldName, (k) -> new ArrayList<>(Arrays.asList(value)));
        } catch (Exception e) {
          throw new MapperException(e.getMessage(), e.getCause());
        }
      }
    }

    return resultMap;
  }

  @Override
  public void execute(TaskContext taskContext) {

    DefaultInput defaultInput = new DefaultInput();
    List<DefaultInput> flatDefaultInput = taskContext.getInput(DefaultInput.class);
    //List<DefaultInput> flatDefaultInput = input1.stream().flatMap(r -> r.stream()).collect(Collectors.toList());
    List<? extends ServiceDocument> documents = flatDefaultInput.stream().map(d -> d.getDocuments()).flatMap(s -> s.stream()).collect(Collectors.toList());

    Map<String, List<Object>> resultMap = new HashMap<>();
    if(flatDefaultInput.size() > 0) {
      defaultInput.setDocuments(documents);
      defaultInput.setExtractDataMembers(flatDefaultInput.get(0).getExtractDataMembers());
      try {
        resultMap = (Map<String, List<Object>>) map(defaultInput);
      } catch (MapperException e) {
        e.printStackTrace();
        //TODO: how to handle failure
      }
    }

    emitOutput(resultMap, taskContext);
  }

  private void emitOutput(Map<String, List<Object>> resultMap,TaskContext taskContext) {

    List<String> outputTaksIds = new ArrayList<String>(taskContext.getTaskDescription().getOutputTaskIds());
    if(outputTaksIds.size() > 1) {
      Map<String, Map<String, List<Object>>> outputSplit = new HashMap<>();
      resultMap.forEach((k,v) -> {
        int i = k.hashCode() % outputTaksIds.size();

        outputSplit.computeIfAbsent(outputTaksIds.get(i), (k1) -> new HashMap<>());
        outputSplit.computeIfPresent(outputTaksIds.get(i), (k1,v1) -> {
          v1.put(k, v);
          return  v1;
        });
      });

      outputSplit.forEach((k,v) -> {
        taskContext.emitOutput(k, v);
      });
    } else if (outputTaksIds.size() == 1){
      taskContext.emitOutput(outputTaksIds.get(0), resultMap);
    } else {
      //Todo: log error and throw exception
    }


  }
}
