package com.vmware.krypton.service.mappers;

/**
 * Created by sivarajm on 11/23/2017.
 */
public interface Mapper <M extends MapInput> {

  Object map(M mapInput) throws MapperException;
}
