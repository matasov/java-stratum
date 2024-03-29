package strat.mining.stratum.proxy.database.repo;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import strat.mining.stratum.proxy.database.PostgresqlManager;
import strat.mining.stratum.proxy.model.User;

public class StratumUserRepositoryImplemented implements StratumUserRepository {

  private String USER_TBL = "stratum_user";
  private static final Logger LOGGER =
      LoggerFactory.getLogger(StratumUserRepositoryImplemented.class);

  @Override
  public void addUser(User user) throws SQLException, IOException {
    LOGGER.debug("try add user: " + user.getName());
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("insert into %1$s values ('%2$s', '%3$s')", USER_TBL, user.getId(),
        new ObjectMapper().writeValueAsString(user));
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public void removeUser(User user) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("delete from %1$s where id = '%2$s'", USER_TBL, user.getId());
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public void updateUser(User user) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    System.out.println("try update user: " + new ObjectMapper().writeValueAsString(user));
    String sql = String.format("update %1$s set json = '%3$s' where %1$s.id = '%2$s'", USER_TBL,
        user.getId(), new ObjectMapper().writeValueAsString(user));
    try {
      workStatement.execute(sql);
    } finally {
      workStatement.close();
    }
  }

  @Override
  public User getUserByID(UUID userID) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s where id = '%2$s'", USER_TBL, userID);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        User row = new ObjectMapper().readValue(rs.getString("json"), User.class);
        return row;
      }
      rs.close();
    } finally {
      if (rs != null)
        rs.close();
      if (workStatement != null)
        workStatement.close();
    }
    return null;
  }

  @Override
  public Map<String, User> getPresentUsers() throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s", USER_TBL);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      if (rs != null) {
        Map<String, User> resultSet = new HashMap<>(rs.getFetchSize());
        while (rs.next()) {
          User row = new ObjectMapper().readValue(rs.getString("json"), User.class);
          resultSet.put(row.getName(), row);
        }
        return resultSet;
      }
    } finally {
      if (rs != null)
        rs.close();
      if (workStatement != null)
        workStatement.close();
    }
    return null;
  }

  @Override
  public User getUserByName(String userName) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("SELECT * FROM %1$s where Lower(json ->> 'name') = Lower('%2$s')",
        USER_TBL, userName);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        User row = new ObjectMapper().readValue(rs.getString("json"), User.class);
        return row;
      }
      rs.close();
    } finally {
      if (rs != null)
        rs.close();
      if (workStatement != null)
        workStatement.close();
    }
    return null;
  }

  @Override
  public void updateUserByName(User user) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    LOGGER.debug("try update user: " + user.getName());
    String sql = String.format(
        "update %1$s set id = '%3$s', json = '%4$s' where Lower(json ->> 'name') = Lower('%2$s')",
        USER_TBL, user.getName(), user.getId(), new ObjectMapper().writeValueAsString(user));
    try {
      workStatement.execute(sql);
    } finally {
      workStatement.close();
    }
  }

}
