package strat.mining.stratum.proxy.database.repo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import strat.mining.stratum.proxy.worker.WorkerConnection;

public interface GetWorkerConnectionRepository {
  void addWorkerConnection(WorkerConnection pool) throws SQLException, IOException;

  void removeWorkerConnection(WorkerConnection wconn) throws SQLException, IOException;

  void updateWorkerConnection(WorkerConnection wconn) throws SQLException, IOException;

  WorkerConnection getWorkerConnectionByID(UUID wconnID) throws SQLException, IOException;
  
  WorkerConnection getWorkerConnectionByName(String wconnName) throws SQLException, IOException;

  List<WorkerConnection> getPresentWorkerConnection() throws SQLException, IOException;

  void updateWorkerConnectionByName(WorkerConnection wconn)
      throws SQLException, IOException;
}
