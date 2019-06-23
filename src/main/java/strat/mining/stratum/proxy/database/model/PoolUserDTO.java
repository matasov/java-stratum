package strat.mining.stratum.proxy.database.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoolUserDTO implements Comparable<PoolUserDTO> {

  UUID id;

  UUID poolID;

  String outIndex;

  String incomingUserName;

  @Override
  public int compareTo(PoolUserDTO compareElement) {
    int selfIntIndex = -1;
    int extIntIndex = -1;
    try {
      selfIntIndex = Integer.parseInt(outIndex);
      extIntIndex = Integer.parseInt(compareElement.getOutIndex());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    if (selfIntIndex >= 0 && extIntIndex >= 0) {
      return selfIntIndex > extIntIndex ? 1 : selfIntIndex == extIntIndex ? 0 : -1;
    } else if (selfIntIndex < 0 && extIntIndex < 0) {
      return outIndex.compareTo(compareElement.getOutIndex());
    } else
      return selfIntIndex < 0 ? -1 : 1;
  }

  @Override
  public String toString() {
    return "{\"id\":\"" + id + "\",\"poolID\":\"" + poolID + "\",\"outIndex\":\"" + outIndex
        + "\",\"incomingUserName\":\"" + incomingUserName + "\"}";
  }

}
