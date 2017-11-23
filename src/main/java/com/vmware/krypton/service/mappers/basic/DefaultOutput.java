package com.vmware.krypton.service.mappers.basic;

import com.vmware.krypton.service.mappers.MapOutput;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by sivarajm on 11/23/2017.
 */
@Setter
@Getter
public class DefaultOutput extends MapOutput {

  Map<String, List<Object>> outputMap;
}
