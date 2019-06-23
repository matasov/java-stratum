package strat.mining.stratum.proxy.manager.strategy;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import strat.mining.stratum.proxy.database.repo.PoolRepository;
import strat.mining.stratum.proxy.database.repo.PoolRepositoryImplemented;
import strat.mining.stratum.proxy.exception.ChangeExtranonceNotSupportedException;
import strat.mining.stratum.proxy.exception.NoPoolAvailableException;
import strat.mining.stratum.proxy.exception.TooManyWorkersException;
import strat.mining.stratum.proxy.manager.ProxyManager;
import strat.mining.stratum.proxy.pool.Pool;
import strat.mining.stratum.proxy.worker.WorkerConnection;

public class PriorityDBStrategyManager extends MonoCurrentPoolStrategyManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(PriorityDBStrategyManager.class);

  public static final String NAME = "priorityDB";

  public static final String DESCRIPTION =
      "Always mine on the highest priority pool (highest priority is 0). Only switch pool when current pool fails or when a higher priority pool is back.";

  private PoolRepository poolRepo;

  public PriorityDBStrategyManager(ProxyManager proxyManager) {
    super(proxyManager);
    poolRepo = new PoolRepositoryImplemented();
    checkConnectionsBinding();
  }

  @Override
  public void onPoolAdded(Pool pool) {
    // If the priority of the pool is set, check if it does not overlap
    // another pool with the same priority
    if (pool.getPriority() == null) {
      // Set by default the priority to the lowest over all pools.
      int minPriority = getMinimumPoolPriority();
      pool.setPriority(minPriority + 1);
    } else {
      // If the priority is set, update the other pools.
      onPoolUpdated(pool);
    }

    // super.onPoolAdded(pool);
    checkConnectionsBinding();

  }

  @Override
  public void onPoolUpdated(Pool poolUpdated) {
    List<Pool> pools = getProxyManager().getPools();
    checkPoolPriorities(pools);
    Collections.sort(pools, new Comparator<Pool>() {
      public int compare(Pool o1, Pool o2) {
        return o1.getPriority().compareTo(o2.getPriority());
      }
    });
    int newPriority = poolUpdated.getPriority();
    int previousPriority = newPriority;
    for (Pool pool : pools) {
      // Move the priority of other pools with lower or
      // equals priority
      if (pool.getPriority() == previousPriority && !pool.equals(poolUpdated)) {
        pool.setPriority(pool.getPriority() + 1);
        previousPriority = pool.getPriority();
      }
    }
    // super.onPoolUpdated(poolUpdated);
    checkConnectionsBinding();
  }

  /**
   * Return the minimal priority over all pools.
   * 
   * @param addPoolDTO
   * @return
   */
  private int getMinimumPoolPriority() {
    int minPriority = 0;
    List<Pool> pools = getProxyManager().getPools();
    for (Pool pool : pools) {
      if (pool.getPriority() != null && pool.getPriority() > minPriority) {
        minPriority = pool.getPriority();
      }
    }
    return minPriority;
  }

  /**
   * Check that all pools have a priority.
   */
  private void checkPoolPriorities(List<Pool> pools) {
    for (Pool pool : pools) {
      if (pool.getPriority() == null) {
        pool.setPriority(getMinimumPoolPriority() + 1);
      }
    }
  }

  /**
   * Compute the and set the current pool. Based on the pool priority and pool state.
   * 
   * @param connection
   * @return
   */
  @Override
  protected void computeCurrentPool() throws NoPoolAvailableException {
    setCurrentPool(computeInternal());
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Map<String, String> getDetails() {
    return super.getDetails();
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public void setParameter(String parameterKey, String value) {
    // No parameters can be changed.
  }

  private Pool computeInternal() throws NoPoolAvailableException {
    List<Pool> pools = getProxyManager().getPools();
    Pool newPool = null;
    checkPoolPriorities(pools);
    Collections.sort(pools, new Comparator<Pool>() {
      public int compare(Pool o1, Pool o2) {
        return o1.getPriority().compareTo(o2.getPriority());
      }
    });
    for (Pool pool : pools) {
      if (pool.getIsReady() && pool.getIsEnabled() && pool.getIsStable()) {
        newPool = pool;
        break;
      }
    }

    if (newPool == null) {
      throw new NoPoolAvailableException("No pool available. " + pools);
    } else {
      return newPool;
    }
  }

  @Override
  protected void checkConnectionsBinding() {
    LOGGER.info("Check all worker connections binding.");
    List<WorkerConnection> workerConnections = proxyManager.getWorkerConnections();
    // Try to rebind connections only if there is at least one connection.

    try {
      Pool oldCurrentPool = getCurrentPool();
      // start update

      List<Pool> pools = getProxyManager().getPools();
      Pool currentPool = computeInternal();
      // computeCurrentPool();
      // end update
      // Pool currentPool = getCurrentPool();
      if (workerConnections.size() > 0) {
        // if (oldCurrentPool != currentPool) {
        LOGGER.info("Switching worker connections from pool {} to pool {}.",
            oldCurrentPool != null ? oldCurrentPool.getName() : "none",
            currentPool != null ? currentPool.getName() : "none");
        for (WorkerConnection connection : workerConnections) {
          // If the connection is not bound to the poolToBind,
          // switch the pool.
          Pool dbPool = null;
          try {
            List<String> userNames = connection.getAuthorizedWorkers().entrySet().stream().limit(1)
                .map(x -> x.getKey()).collect(Collectors.toList());
            if (userNames == null || userNames.isEmpty()) {
              throw new NoPoolAvailableException();
            }
            dbPool = poolRepo.getPoolByUserNameStrategy(userNames.get(0));
            // dbPool = poolRepo.getPoolByConnectionIdStrategy(connection.getId());
            if (dbPool != null)
              for (Pool currentManagerPool : pools) {
                if (currentManagerPool.getId().equals(dbPool.getId())
                    && currentManagerPool.getIsReady() && currentManagerPool.getIsEnabled()
                    && currentManagerPool.getIsStable()) {
                  dbPool = currentManagerPool;
                }
              }
          } catch (SQLException | IOException ex) {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            LOGGER.error(errors.toString());
            ex.printStackTrace();
          }
          if (dbPool != null)
            currentPool = dbPool;
          if (!connection.getPool().equals(currentPool)) {
            try {
              LOGGER.info("for connection {} found pool {}.",
                  connection != null
                      ? (connection.getConnectionName() + "[" + connection.getId() + "] ")
                      : "none",
                  currentPool != null ? currentPool.getName() : "none");
              proxyManager.switchPoolForConnection(connection, currentPool);
            } catch (TooManyWorkersException e) {
              LOGGER.warn(
                  "Failed to rebind worker connection {} on pool {}. Too many workers on this pool.",
                  connection.getConnectionName(), currentPool.getName());
            } catch (ChangeExtranonceNotSupportedException e) {
              LOGGER.info(
                  "Close connection {} since the on-the-fly extranonce change is not supported.",
                  connection.getConnectionName(), currentPool.getName());
              connection.close();
              proxyManager.onWorkerDisconnection(connection, e);
            } catch (Exception ex) {
              StringWriter errors = new StringWriter();
              ex.printStackTrace(new PrintWriter(errors));
              LOGGER.error(errors.toString());
              ex.printStackTrace();
            }
          }
        }
        // }
      }

    } catch (NoPoolAvailableException e) {
      // Close worker connections only if there are worker connections (of
      // course, so obvious...nnnaaaaarrrrhhhh)
      if (workerConnections.size() > 0) {
        LOGGER.error(
            "Failed to rebind workers connections. No pool is available. Closing all workers connections.",
            e);
        // If no more pool available, close all worker connections
        proxyManager.closeAllWorkerConnections();
      }
      setCurrentPool(null);
    }
  }

  @Override
  public Pool getPoolForConnection(WorkerConnection connection) throws NoPoolAvailableException {
    LOGGER.warn("try search pool for connection: " + connection.getId());
    Pool presentedPool = null;
    try {
      // presentedPool = poolRepo.getPoolByConnectionIdStrategy(connection.getId());
      List<String> userNames = connection.getAuthorizedWorkers().entrySet().stream().limit(1)
          .map(x -> x.getKey()).collect(Collectors.toList());
      if (userNames == null || userNames.isEmpty()) {
        throw new NoPoolAvailableException();
      }
      presentedPool = poolRepo.getPoolByUserNameStrategy(userNames.get(0));
      List<Pool> pools = getProxyManager().getPools();
      if (presentedPool != null)
        for (Pool current : pools) {
          if (current.getId().equals(presentedPool.getId()) && current.getIsReady()
              && current.getIsEnabled() && current.getIsStable()) {
            presentedPool = current;
            break;
          }
        }
      computeCurrentPool();
    } catch (Exception ex) {
      StringWriter errors = new StringWriter();
      ex.printStackTrace(new PrintWriter(errors));
      LOGGER.error(errors.toString());
      ex.printStackTrace();
      throw new NoPoolAvailableException();
    }

    LOGGER.warn("found pool: " + (presentedPool == null ? getCurrentPool() : presentedPool));
    return presentedPool == null ? getCurrentPool() : presentedPool;
  }

}
