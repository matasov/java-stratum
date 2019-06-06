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

public class PoolRepositoryImplemented implements PoolRepository {
  private String POOL_TBL = "pool";
  private String POOL_RELATION_TBL = "connection_pool_relations";

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

  public Pool getPoolByID(UUID poolID) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s where id = '%2$s'", POOL_TBL, poolID);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        Pool row = new ObjectMapper().readValue(rs.getString("pool"), Pool.class);
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

  public List<Pool> getPresentPools() throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s", POOL_TBL);
    System.out.println("query: " + sql);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      if (rs != null) {
        System.out.println("found pools: " + rs.getFetchSize());
        List<Pool> resultSet = new ArrayList<>(rs.getFetchSize());
        while (rs.next()) {
          System.out.println("pool json: " + rs.getString("pool"));
          Pool row = new ObjectMapper().readValue(rs.getString("pool"), Pool.class);
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
  public Pool getPoolByConnectionIdStrategy(UUID connectionID) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format(
        "select * from %1$s where id = (select pool from %2$s where connection = '%3$s')",
        POOL_TBL, POOL_RELATION_TBL, connectionID);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        Pool row = new ObjectMapper().readValue(rs.getString("pool"), Pool.class);
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
}
