package strat.mining.stratum.proxy.pool;

import strat.mining.stratum.proxy.database.model.PoolUserDTO;

public interface PoolUsersManager {
  
  String getUsername();
  
  String getPassword();
  
  PoolUserDTO getPoolUserDTOFromRequest(Pool pool, String requestName);  
  
  PoolUserDTO getPoolUserDTOFromRespond(Pool pool, String outerIndex);  
    
}
