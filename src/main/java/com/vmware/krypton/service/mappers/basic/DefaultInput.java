package com.vmware.krypton.service.mappers.basic;

import com.vmware.krypton.service.mappers.MapInput;
import com.vmware.xenon.common.ServiceDocument;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by sivarajm on 11/23/2017.
 */

@Getter
@Setter
@AllArgsConstructor
public class DefaultInput extends MapInput {
  List<? extends ServiceDocument> documents;
  private List<String> extractDataMembers;
}
