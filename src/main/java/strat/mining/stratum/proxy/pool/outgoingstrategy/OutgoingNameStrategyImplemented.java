package strat.mining.stratum.proxy.pool.outgoingstrategy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import strat.mining.stratum.proxy.database.model.PoolUserDTO;
import strat.mining.stratum.proxy.database.repo.PoolUserRelationRepositoryImplemented;


public class OutgoingNameStrategyImplemented implements OutgoingNameStrategy {

  private PoolUserRelationRepositoryImplemented poolUserRelationRepositoryImplemented;

  public OutgoingNameStrategyImplemented() {
    poolUserRelationRepositoryImplemented = new PoolUserRelationRepositoryImplemented();
  }

  @Override
  public PoolUserDTO getUserDTO(UUID poolID, String incomingUserName) {
    PoolUserDTO result = null;
    try {
      result = poolUserRelationRepositoryImplemented.getPoolUserByIncomingUserIdStrategy(poolID,
          incomingUserName);
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    }
    if (result == null) {
      String outIndex = getPoolUserOutIndexByStrategy(poolID);
      result = new PoolUserDTO(UUID.randomUUID(), poolID, outIndex, incomingUserName);
      try {
        poolUserRelationRepositoryImplemented.addPoolUserDTO(result);
      } catch (SQLException | IOException e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  private String getPoolUserOutIndexByStrategy(UUID poolID) {
    List<PoolUserDTO> presentUsers = null;
    try {
      presentUsers = poolUserRelationRepositoryImplemented.getPresentUsersForPool(poolID);
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    }
    // if is empty
    if (presentUsers == null || presentUsers.isEmpty()) {
      return "0";
    } else {
      // boolean isFound = false;
      int[] increment = {0, -1};
      // List<Integer> values =
      presentUsers.stream().map(x -> getIntFromOutIndex(x.getOutIndex())).filter(x -> x > -1)
          .sorted().forEach(x -> {
            if (increment[1] < 0) {
              if (x == increment[0]) {
                increment[0]++;
              } else {
                increment[1] = increment[0];
              }
            }
          });
      return increment[1] < 0 ? "0" : Integer.toString(increment[1]);
    }
  }

  private int getIntFromOutIndex(String outIndex) {
    try {
      return Integer.parseInt(outIndex);
    } catch (Exception ex) {
      return -1;
    }
  }

}
