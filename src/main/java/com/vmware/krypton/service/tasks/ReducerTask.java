package com.vmware.krypton.service.tasks;

import com.vmware.krypton.model.Task;
import com.vmware.krypton.model.TaskContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by nibunangs on 23-Nov-2017.
 */
public class ReducerTask implements Task {

    @Override
    public void execute(TaskContext taskContext) {
        Collection<Map<String, List<String>>> input = taskContext.getInput();

        Map<String, List<String>> finalMap = new HashMap<>();
        input.forEach(m -> {
            m.entrySet().forEach((e) -> {
                finalMap.merge(e.getKey(), e.getValue(), (l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                });
            });
        });

        Map<String, Object> aggregatedMap = aggregateEntires(finalMap, "sum");
        emitOutput(aggregatedMap, taskContext);
    }

    private Map<String, Object> aggregateEntires(Map<String, List<String>> finalMap, String sum) {
        switch (sum) {
            case "sum" : return sumAggregation(finalMap);
        }
        return null;
    }

    private Map<String, Object> sumAggregation(Map<String, List<String>> finalMap) {
        return finalMap.entrySet().stream().collect(Collectors.toMap(o -> o.getKey(), t ->
                t.getValue().stream().mapToInt(value -> Integer.parseInt(value)).sum()));
    }

    private void emitOutput(Map<String, Object> resultMap,TaskContext taskContext) {

        List<String> outputTaksIds = new ArrayList<String>(taskContext.getTaskDescription().getOutputTaskIds());
        if(outputTaksIds.size() == 1) {
            taskContext.emitOutput(outputTaksIds.get(0), resultMap);
        } else {
            //Todo: log error and throw exception
        }


    }
//For Testing Reducer function
//    public static void main(String[] args) {
//        Collection<Map<String, List<String>>> input = new ArrayList<>();
//        Map<String, List<String>> m1 = new HashMap<>();
//        Map<String, List<String>> m2 = new HashMap<>();
//        m1.put("a", Arrays.asList("1","2" , "3"));
//        m1.put("b", Arrays.asList("12","3","4"));
//        m2.put("a", Arrays.asList("4", "5" , "6"));
//        m2.put("b", Arrays.asList("4","5"));
//        input.add(m1);
//        input.add(m2);
//
//        Map<String, List<String>> finalMap = new HashMap<>();
//
//        input.stream().forEach(m -> {
//            m.entrySet().forEach((e) -> {
//                finalMap.merge(e.getKey(), e.getValue(), (l1, l2) -> {
//                    ArrayList<String> l3 = new ArrayList<>();
//                    l3.addAll(l1);
//                    l3.addAll(l2);
//                    return l3;
//                });
//            });
//        });
//
//        Map<String, Object> aggregatedMap = aggregateEntires(finalMap, "sum");
//
//    }
}
