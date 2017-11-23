package com.vmware.krypton.service.mappers.basic;

import com.vmware.krypton.service.mappers.MapContext;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by sivarajm on 11/23/2017.
 */

@Getter
@Setter
public class DefaultContext extends MapContext<DefaultOutput> {
  private List<String> extractDataMembers;

  @Override
  public void emmit(DefaultOutput resultMap) {

  }
}
