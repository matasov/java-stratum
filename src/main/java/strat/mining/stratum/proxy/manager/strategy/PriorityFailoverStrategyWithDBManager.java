package strat.mining.stratum.proxy.manager.strategy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import strat.mining.stratum.proxy.database.repo.PoolRepository;
import strat.mining.stratum.proxy.database.repo.PoolRepositoryImplemented;
import strat.mining.stratum.proxy.exception.NoPoolAvailableException;
import strat.mining.stratum.proxy.manager.ProxyManager;
import strat.mining.stratum.proxy.pool.Pool;
import strat.mining.stratum.proxy.worker.WorkerConnection;

public class PriorityFailoverStrategyWithDBManager extends MonoCurrentPoolStrategyManager {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(PriorityFailoverStrategyWithDBManager.class);
  public static final String NAME = "priorityDB";

  public static final String DESCRIPTION =
      "Start search DB record. Always mine on the highest priority pool (highest priority is 0). Only switch pool when current pool fails or when a higher priority pool is back.";

  private PoolRepository poolRepo;

  public PriorityFailoverStrategyWithDBManager(ProxyManager proxyManager) {
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

    super.onPoolAdded(pool);

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

    super.onPoolUpdated(poolUpdated);
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
      setCurrentPool(newPool);
    }
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

  @Override
  public Pool getPoolForConnection(WorkerConnection connection) throws NoPoolAvailableException {
    LOGGER.warn("try search pool for connection: " + connection.getId());
    Pool presentedPool = null;
    try {
      presentedPool = poolRepo.getPoolByConnectionIdStrategy(connection.getId());
      List<Pool> pools = getProxyManager().getPools();
      if (presentedPool != null)
        for (Pool current : pools) {
          if (current.getId().equals(presentedPool.getId())) {
            presentedPool = current;
            break;
          }
        }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    if (presentedPool == null) {
      computeCurrentPool();
    } else {
      setCurrentPool(presentedPool);
    }
    return getCurrentPool();
  }

}
