package com.vmware.krypton.service.tasks;

public class CountMapper extends MapperTask<String, Object, String, Integer> {
    @Override
    public void map(String key, Object value) {
        emit("", 1);
    }
}
