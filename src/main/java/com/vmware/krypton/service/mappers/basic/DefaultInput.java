package com.vmware.krypton.service.mappers.basic;

import com.vmware.krypton.host.TestService.TestState;
import com.vmware.krypton.service.mappers.MapInput;
import com.vmware.xenon.common.ServiceDocument;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by sivarajm on 11/23/2017.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DefaultInput extends MapInput {
  List<TestState> documents;
  private List<String> extractDataMembers;

  @Override
  public String toString() {
    return "documentCount=" + documents.size();
  }
}
