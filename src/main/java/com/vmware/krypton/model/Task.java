package com.vmware.krypton.model;

public interface Task <I,O> {

    void execute(TaskContext<I, O> taskContext) throws NoSuchFieldException;

}
