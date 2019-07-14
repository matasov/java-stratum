package strat.mining.stratum.proxy.database.repo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import strat.mining.stratum.proxy.database.model.PoolUserDTO;

public interface PoolUserRelationRepository {

  void addPoolUserDTO(PoolUserDTO pool) throws SQLException, IOException;

  void removePoolUserDTO(PoolUserDTO pool) throws SQLException, IOException;

  void updatePoolUserDTO(PoolUserDTO pool) throws SQLException, IOException;

  PoolUserDTO getPoolUserDTOByID(UUID PoolUserDTOID) throws SQLException, IOException;
  
  List<PoolUserDTO> getAllPresentUsers() throws SQLException, IOException;

  List<PoolUserDTO> getPresentUsersForPool(UUID poolID) throws SQLException, IOException;

  PoolUserDTO getPoolUserByIncomingUserIdStrategy(UUID poolID, String userName)
      throws SQLException, IOException;

  PoolUserDTO getPoolUserByOutgoingUserIdStrategy(UUID poolID, String userIndex)
      throws SQLException, IOException;
}
