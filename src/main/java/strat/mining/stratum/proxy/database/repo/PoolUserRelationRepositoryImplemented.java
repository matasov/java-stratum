package strat.mining.stratum.proxy.database.repo;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import strat.mining.stratum.proxy.database.PostgresqlManager;
import strat.mining.stratum.proxy.database.model.PoolUserDTO;

public class PoolUserRelationRepositoryImplemented implements PoolUserRelationRepository {
  private String PROXY_POOL_RELATION_TBL = "proxy_pool_relations";
  private String POOL_TBL = "pool";
  private static String SITE_USER_TBL = "site_user";

  @Override
  public void addPoolUserDTO(PoolUserDTO pool) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("insert into %1$s values ('%2$s', '%3$s', '%4$s', '%5$s')",
        PROXY_POOL_RELATION_TBL, pool.getId(), pool.getPoolID(), pool.getOutIndex().toLowerCase(),
        pool.getIncomingUserName().toLowerCase());
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public void removePoolUserDTO(PoolUserDTO pool) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql =
        String.format("delete from %1$s where id = '%2$s'", PROXY_POOL_RELATION_TBL, pool.getId());
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public void updatePoolUserDTO(PoolUserDTO pool) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format(
        "update %1$s set pool_id = '%3$s', out_index = '%4$s', incoming_name = '%5$s' where id = '%2$s'",
        PROXY_POOL_RELATION_TBL, pool.getId(), pool.getPoolID(), pool.getOutIndex().toLowerCase(),
        pool.getIncomingUserName().toLowerCase());
    System.out.println("value for: "+sql);
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public PoolUserDTO getPoolUserDTOByID(UUID PoolUserDTOID) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s where id = '%2$s'", PROXY_POOL_RELATION_TBL,
        PoolUserDTOID);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        PoolUserDTO row = new PoolUserDTO();
        row.setId(UUID.fromString(rs.getString("id")));
        row.setPoolID(UUID.fromString(rs.getString("pool_id")));
        row.setOutIndex(rs.getString("out_index"));
        row.setIncomingUserName(rs.getString("incoming_name"));
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
  public List<PoolUserDTO> getPresentUsersForPool(UUID poolID) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql =
        String.format("select * from %1$s where pool_id = '%2$s' order by out_index", PROXY_POOL_RELATION_TBL, poolID);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      if (rs != null) {
        List<PoolUserDTO> resultSet = new ArrayList<>(rs.getFetchSize());
        while (rs.next()) {
          PoolUserDTO row = new PoolUserDTO();
          row.setId(UUID.fromString(rs.getString("id")));
          row.setPoolID(UUID.fromString(rs.getString("pool_id")));
          row.setOutIndex(rs.getString("out_index"));
          row.setIncomingUserName(rs.getString("incoming_name"));
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
  public PoolUserDTO getPoolUserByIncomingUserIdStrategy(UUID poolID, String userName)
      throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql =
        String.format("select * from %1$s where pool_id = '%2$s' and incoming_name = '%3$s'",
            PROXY_POOL_RELATION_TBL, poolID, userName);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        PoolUserDTO row = new PoolUserDTO();
        row.setId(UUID.fromString(rs.getString("id")));
        row.setPoolID(UUID.fromString(rs.getString("pool_id")));
        row.setOutIndex(rs.getString("out_index"));
        row.setIncomingUserName(rs.getString("incoming_name"));
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
  public PoolUserDTO getPoolUserByOutgoingUserIdStrategy(UUID poolID, String userIndex)
      throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s where pool_id = '%2$s' and out_index = '%3$s'",
        PROXY_POOL_RELATION_TBL, poolID, userIndex);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        PoolUserDTO row = new PoolUserDTO();
        row.setId(UUID.fromString(rs.getString("id")));
        row.setPoolID(UUID.fromString(rs.getString("pool_id")));
        row.setOutIndex(rs.getString("out_index"));
        row.setIncomingUserName(rs.getString("incoming_name"));
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
