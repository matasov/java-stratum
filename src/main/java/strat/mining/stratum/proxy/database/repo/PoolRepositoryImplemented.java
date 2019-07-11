package strat.mining.stratum.proxy.database.repo;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import strat.mining.stratum.proxy.database.PostgresqlManager;
import strat.mining.stratum.proxy.pool.Pool;
import strat.mining.stratum.proxy.worker.StratumWorkerConnection;
import strat.mining.stratum.proxy.worker.WorkerConnection;

public class PoolRepositoryImplemented implements PoolRepository {
  private String POOL_TBL = "pool";
  private String POOL_RELATION_TBL = "connection_pool_relations";
  private String POOL_USERNAME_RELATION_TBL = "username_pool_relations";

  public void addPool(Pool pool) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("insert into %1$s values ('%2$s', '%3$s')", POOL_TBL, pool.getId(),
        new ObjectMapper().writeValueAsString(pool));
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  public void removePool(Pool pool) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("delete from %1$s where id = '%2$s'", POOL_TBL, pool.getId());
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public void updatePool(Pool pool) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("update %1$s set pool = '%3$s' where id = '%2$s'", POOL_TBL,
        pool.getId(), new ObjectMapper().writeValueAsString(pool));
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public void updatePoolByHost(Pool pool) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    System.out.println("try update pool: " + new ObjectMapper().writeValueAsString(pool));
    String sql = String.format(
        "update %1$s set id = '%3$s', poll = '%4$s' where Lower(pool ->> 'host') = Lower('%2$s')",
        POOL_TBL, pool.getHost(), pool.getId(), new ObjectMapper().writeValueAsString(pool));
    try {
      workStatement.execute(sql);
    } finally {
      workStatement.close();
    }
  }

  @Override
  public Pool getPoolByID(UUID poolID) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s where id = '%2$s'", POOL_TBL, poolID);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        Pool row = new ObjectMapper().readValue(rs.getString("pool"), Pool.class);
        row.setId(UUID.fromString(rs.getString("id")));
        row.setAppendWorkerNames(true);
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
  public List<Pool> getPresentPools() throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s", POOL_TBL);
    System.out.println("query: " + sql);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      if (rs != null) {
        List<Pool> resultSet = new ArrayList<>(5);
        while (rs.next()) {
          System.out.println("pool json: " + rs.getString("pool"));
          Pool row = new ObjectMapper().readValue(rs.getString("pool"), Pool.class);
          row.setId(UUID.fromString(rs.getString("id")));
          row.setAppendWorkerNames(true);
          resultSet.add(row);
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
  public Pool getPoolByHost(String host) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s where Lower(pool ->> 'host') = Lower('%2$s')",
        POOL_TBL, host);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        Pool row = new ObjectMapper().readValue(rs.getString("pool"), Pool.class);
        row.setId(UUID.fromString(rs.getString("id")));
        row.setAppendWorkerNames(true);
        // Start the getwork timeout
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
  public Pool getPoolByName(String name) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s where Lower(pool ->> 'name') = Lower('%2$s')",
        POOL_TBL, name);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        Pool row = new ObjectMapper().readValue(rs.getString("pool"), Pool.class);
        row.setId(UUID.fromString(rs.getString("id")));
        row.setAppendWorkerNames(true);
        // Start the getwork timeout
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
  public Pool getPoolByConnectionIdStrategy(UUID connectionID) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format(
        "select * from %1$s where id = (select pool from %2$s where connection = '%3$s')", POOL_TBL,
        POOL_RELATION_TBL, connectionID);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        Pool row = new ObjectMapper().readValue(rs.getString("pool"), Pool.class);
        row.setId(UUID.fromString(rs.getString("id")));
        row.setAppendWorkerNames(true);
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
  public Pool getPoolByUserNameStrategy(String userName) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format(
        "select * from %1$s where id = (select pool from %2$s where LOWER(user_name) = LOWER('%3$s'))",
        POOL_TBL, POOL_USERNAME_RELATION_TBL, userName);
    System.out.println("getPoolByUserNameStrategy: " + sql);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        Pool row = new ObjectMapper().readValue(rs.getString("pool"), Pool.class);
        row.setId(UUID.fromString(rs.getString("id")));
        row.setAppendWorkerNames(true);
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

  public UUID getUUIDRecordByUserNameStrategy(String userName) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format(
        "select * from %2$s where user_name = '%3$s' limit 1",
        POOL_TBL, POOL_USERNAME_RELATION_TBL, userName);
    System.out.println("getUUIDRecordByUserNameStrategy: " + sql);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        return UUID.fromString(rs.getString("id"));
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
  public void addPoolByUserNameStrategy(String userName, UUID poolID)
      throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("insert into %1$s values ('%2$s', '%3$s', '%4$s')",
        POOL_USERNAME_RELATION_TBL, UUID.randomUUID(), userName.toLowerCase(), poolID);
    System.out.println("addPoolByUserNameStrategy: " + sql);
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public void updatePoolByUserNameStrategy(String recordID, String userName, UUID poolID)
      throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql =
        String.format("update %1$s set user_name = '%3$s', pool = '%4$s' where id = '%2$s'",
            POOL_USERNAME_RELATION_TBL, recordID, userName.toLowerCase(), poolID);
    System.out.println("updatePoolByUserNameStrategy sql: " + sql);
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }
}
