package strat.mining.stratum.proxy.database.model;

import lombok.Data;

@Data
public class SitePoolUserDTO extends PoolUserDTO {

  String connectionName;

  double accessHash;

  double rejectHash;

  public SitePoolUserDTO() {
    super();
  }
  
  public SitePoolUserDTO(PoolUserDTO parentDTO) {
    super();
    setId(parentDTO.getId());
    setPoolID(parentDTO.getPoolID());
    setPoolName(parentDTO.getPoolName());
    setOutIndex(parentDTO.getOutIndex());
    setIncomingUserName(parentDTO.getIncomingUserName());
  }

  @Override
  public String toString() {
    return "{\"id\":\"" + id + "\",\"poolID\":\"" + poolID + "\",\"poolName\":\"" + getPoolName()
        + "\", \"outIndex\":\"" + outIndex + "\",\"incomingUserName\":\"" + incomingUserName
        + "\",\"connectionName\":\"" + connectionName + "\",\"accessHash\":\"" + accessHash
        + "\",\"rejectHash\":\"" + rejectHash + "\"}";
  }

}
