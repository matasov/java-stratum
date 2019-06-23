package strat.mining.stratum.proxy.pool.outgoingstrategy;

import java.util.UUID;
import strat.mining.stratum.proxy.database.model.PoolUserDTO;

public interface OutgoingNameStrategy {
  PoolUserDTO getUserDTO(UUID poolID, String incomingUserName);
}
