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
import strat.mining.stratum.proxy.worker.GetworkWorkerConnection;
import strat.mining.stratum.proxy.worker.WorkerConnection;

public class GetWorkerConnectionRepositoryImplemented implements GetWorkerConnectionRepository {
  private String CONNECTION_TBL = "connection";

  @Override
  public void addWorkerConnection(WorkerConnection wconn) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("insert into %1$s values ('%2$s', '%3$s')", CONNECTION_TBL,
        wconn.getId(), new ObjectMapper().writeValueAsString(wconn));
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public void removeWorkerConnection(WorkerConnection wconn) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("delete from %1$s where id = '%2$s'", CONNECTION_TBL, wconn.getId());
    try {
      workStatement.execute(sql);
    } finally {
      if (workStatement != null)
        workStatement.close();
    }
  }

  @Override
  public void updateWorkerConnection(WorkerConnection wconn) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("update %1$s set pool = '%3$s' where id = '%2$s'", CONNECTION_TBL,
        wconn.getId(), new ObjectMapper().writeValueAsString(wconn));
    try {
      workStatement.execute(sql);
    } finally {
      workStatement.close();
    }
  }

  @Override
  public WorkerConnection getWorkerConnectionByID(UUID wconnID) throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s where id = '%2$s'", CONNECTION_TBL, wconnID);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      while (rs.next()) {
        WorkerConnection row =
            new ObjectMapper().readValue(rs.getString("connection"), WorkerConnection.class);
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
  public List<WorkerConnection> getPresentWorkerConnection() throws SQLException, IOException {
    Statement workStatement = PostgresqlManager.getConnection().createStatement();
    String sql = String.format("select * from %1$s", CONNECTION_TBL);
    ResultSet rs = null;
    try {
      rs = workStatement.executeQuery(sql);
      if (rs != null) {
        List<WorkerConnection> resultSet = new ArrayList<>(rs.getFetchSize());
        while (rs.next()) {
          WorkerConnection row =
              new ObjectMapper().readValue(rs.getString("connection"), GetworkWorkerConnection.class);
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
  public WorkerConnection getWorkerConnectionByUserIdStrategy(UUID wconnID)
      throws SQLException, IOException {
    // TODO Auto-generated method stub
    return null;
  }

}
