package com.vmware.krypton.model;

import com.google.common.collect.ImmutableMap;
import com.vmware.xenon.common.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nibunangs on 24-Nov-2017.
 */
public class TaskContextTest {

    private Map<String, List<Integer>> mapperTypeToken;

    @Test
    public void testGetInput() throws Exception {
        Map<String, List<Object>> mapperOutput = new HashMap<>();
        mapperOutput.put("IDLE_DISK", Arrays.asList(100, 200));
        mapperOutput.put("POWERED_OFF", Arrays.asList(3000));
        String mapperOutputJson = Utils.toJson(mapperOutput);

        TaskContext context = new TaskContext();
        context.setInputTaskIdToCompletionMap(ImmutableMap.of("1", Boolean.TRUE));
        context.setInputTaskIdToDataMap(ImmutableMap.of("1", mapperOutputJson));

        Type mapperInputType = TaskContextTest.class.getDeclaredField("mapperTypeToken").getGenericType();
        List input = context.getInput(mapperInputType);
        Assert.assertEquals(1, input.size());
    }

}