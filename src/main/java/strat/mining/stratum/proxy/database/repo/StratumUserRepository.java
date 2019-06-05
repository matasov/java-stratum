package strat.mining.stratum.proxy.database.repo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import strat.mining.stratum.proxy.model.User;

public interface StratumUserRepository {
  void addUser(User user) throws SQLException, IOException;

  void removeUser(User user) throws SQLException, IOException;

  void updateUser(User user) throws SQLException, IOException;

  User getUserByID(UUID userID) throws SQLException, IOException;

  Map<String, User> getPresentUsers() throws SQLException, IOException;
}
