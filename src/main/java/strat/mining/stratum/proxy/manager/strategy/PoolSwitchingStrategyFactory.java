/**
 * stratum-proxy is a proxy supporting the crypto-currency stratum pool mining protocol. Copyright
 * (C) 2014-2015 Stratehm (stratehm@hotmail.com)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * multipool-stats-backend. If not, see <http://www.gnu.org/licenses/>.
 */
package strat.mining.stratum.proxy.manager.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import strat.mining.stratum.proxy.exception.UnsupportedPoolSwitchingStrategyException;
import strat.mining.stratum.proxy.manager.ProxyManager;
import strat.mining.stratum.proxy.network.StratumConnection;

public class PoolSwitchingStrategyFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(PoolSwitchingStrategyFactory.class);

  private ProxyManager proxyManager;

  public PoolSwitchingStrategyFactory(ProxyManager proxyManager) {
    this.proxyManager = proxyManager;
  }

  public PoolSwitchingStrategyManager getPoolSwitchingStrategyManagerByName(String name)
      throws UnsupportedPoolSwitchingStrategyException {
    LOGGER.warn("connect to strategy.");
    PoolSwitchingStrategyManager result = getPriorityFailoverStrategyWithDBManager();
    if (result != null)
      return result;
    if (PriorityFailoverStrategyWithDBManager.NAME.equalsIgnoreCase(name)) {
      result = getPriorityFailoverStrategyWithDBManager();
    } else if (PriorityFailoverStrategyManager.NAME.equalsIgnoreCase(name)) {
      result = getPriorityFailoverStrategyManager();
    } else if (WeightedRoundRobinStrategyManager.NAME.equalsIgnoreCase(name)) {
      result = getWeightedRoundRobinStrategyManager();
    } else {
      throw new UnsupportedPoolSwitchingStrategyException(
          "No pool switching strategy found with name " + name
              + ". Available strategy are: priorityFailover, weightedRoundRobin");
    }
    return result;
  }

  private PriorityFailoverStrategyManager getPriorityFailoverStrategyManager() {
    return new PriorityFailoverStrategyManager(proxyManager);
  }

  private WeightedRoundRobinStrategyManager getWeightedRoundRobinStrategyManager() {
    return new WeightedRoundRobinStrategyManager(proxyManager);
  }

  private PriorityFailoverStrategyWithDBManager getPriorityFailoverStrategyWithDBManager() {
    return new PriorityFailoverStrategyWithDBManager(proxyManager);
  }
}
