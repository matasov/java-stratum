package strat.mining.stratum.proxy.database.repo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import strat.mining.stratum.proxy.database.model.PoolUserDTO;
import strat.mining.stratum.proxy.pool.Pool;

public interface PoolRepository {
  void addPool(Pool pool) throws SQLException, IOException;

  void removePool(Pool pool) throws SQLException, IOException;

  void updatePool(Pool pool) throws SQLException, IOException;
  
  void updatePoolByHost(Pool pool) throws SQLException, IOException;

  Pool getPoolByID(UUID PoolID) throws SQLException, IOException;
  
  Pool getPoolByName(String PoolName) throws SQLException, IOException;

  List<Pool> getPresentPools() throws SQLException, IOException;
  
  Pool getPoolByHost(String host) throws SQLException, IOException;

  Pool getPoolByConnectionIdStrategy(UUID userID) throws SQLException, IOException;
  
  Pool getPoolByUserNameStrategy(String userName) throws SQLException, IOException;
  
  UUID getUUIDRecordByUserNameStrategy(String userName) throws SQLException, IOException;
  
  void addPoolByUserNameStrategy(String userName, UUID poolID) throws SQLException, IOException;
  
  void updatePoolByUserNameStrategy(String recordID, String userName, UUID poolID) throws SQLException, IOException;
}
