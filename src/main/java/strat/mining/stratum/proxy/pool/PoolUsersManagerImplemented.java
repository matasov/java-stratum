package strat.mining.stratum.proxy.pool;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import strat.mining.stratum.proxy.database.model.PoolUserDTO;
import strat.mining.stratum.proxy.database.repo.PoolUserRelationRepositoryImplemented;
import strat.mining.stratum.proxy.manager.ProxyManager;
import strat.mining.stratum.proxy.pool.outgoingstrategy.OutgoingNameStrategy;
import strat.mining.stratum.proxy.pool.outgoingstrategy.OutgoingNameStrategyImplemented;

public class PoolUsersManagerImplemented implements PoolUsersManager {

  private PoolUserRelationRepositoryImplemented poolUserRelationRepositoryImplemented;
  private OutgoingNameStrategy outStrategy;

  // private LinkedSet

  public PoolUsersManagerImplemented() {
    outStrategy = new OutgoingNameStrategyImplemented();
  }

  @Override
  public String getUsername() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPassword() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PoolUserDTO getPoolUserDTOFromRequest(Pool pool, String requestName) {
    return outStrategy.getUserDTO(pool.getId(), requestName);
  }

  @Override
  public PoolUserDTO getPoolUserDTOFromRespond(Pool pool, String outerIndex) {
    try {
      return poolUserRelationRepositoryImplemented.getPoolUserByOutgoingUserIdStrategy(pool.getId(),
          outerIndex);
    } catch (SQLException | IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
