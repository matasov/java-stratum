package strat.mining.stratum.proxy.pool;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PoolUser {
  UUID poolID;
  UUID userID;
  String userSuffix;
  UUID id;
  String index;
}
