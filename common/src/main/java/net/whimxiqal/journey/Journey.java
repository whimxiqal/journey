/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.whimxiqal.journey;

import net.whimxiqal.journey.data.DataManager;
import net.whimxiqal.journey.data.DataManagerImpl;
import net.whimxiqal.journey.data.DataVersion;
import net.whimxiqal.journey.manager.TunnelManager;
import net.whimxiqal.journey.scope.ScopeManager;
import net.whimxiqal.journey.manager.DebugManager;
import net.whimxiqal.journey.manager.NetherManager;
import net.whimxiqal.journey.manager.PlayerManager;
import net.whimxiqal.journey.manager.SearchManager;
import net.whimxiqal.journey.search.event.SearchDispatcher;
import net.whimxiqal.journey.search.event.SearchDispatcherImpl;
import net.whimxiqal.journey.stats.StatsManager;
import net.whimxiqal.journey.util.BStatsUtil;
import net.whimxiqal.journey.util.CommonLogger;
import net.whimxiqal.journey.util.Initializable;

public final class Journey {

  public static final String NAME = "Journey";
  private static final Journey instance = new Journey();
  private final SearchDispatcherImpl searchEventDispatcher = new SearchDispatcherImpl();
  private final PlayerManager playerManager = new PlayerManager();
  private final DebugManager debugManager = new DebugManager();
  private final NetherManager netherManager = new NetherManager();
  private final SearchManager searchManager = new SearchManager();
  private final ScopeManager scopeManager = new ScopeManager();
  private final TunnelManager tunnelManager = new TunnelManager();
  private final StatsManager statsManager = new StatsManager();
  private DataManager dataManager = new DataManagerImpl();
  private Proxy proxy;

  public static CommonLogger logger() {
    return instance.proxy.logger();
  }

  public static Journey get() {
    return instance;
  }

  public Proxy proxy() {
    return proxy;
  }

  public void registerProxy(Proxy proxy) {
    if (this.proxy != null) {
      throw new IllegalStateException("The proxy was already registered.");
    }
    this.proxy = proxy;
  }

  public void init() {
    JourneyApiSupplier.set(new JourneyApiImpl());
    if (dataManager instanceof Initializable) {
      ((Initializable) dataManager).initialize();
    }
    netherManager.load();
    searchEventDispatcher.initialize();
    searchManager.initialize();
    scopeManager.initialize();
    statsManager.initialize();
    BStatsUtil.register(proxy.platform().bStatsChartConsumer());

    // Database version -- not yet relevant since we haven't needed to migrate data
    if (dataManager.version() != DataVersion.V001) {
      logger().error("The Journey database is using an invalid version.");
    }
  }

  public void shutdown() {
    searchEventDispatcher.shutdown();
    searchManager.shutdown();
    proxy.audienceProvider().close();
    statsManager.shutdown();
  }

  public DataManager dataManager() {
    return dataManager;
  }

  public void setDataManager(DataManager dataManager) {
    this.dataManager = dataManager;
  }

  public SearchDispatcher dispatcher() {
    return searchEventDispatcher;
  }

  public PlayerManager deathManager() {
    return playerManager;
  }

  public DebugManager debugManager() {
    return debugManager;
  }

  public NetherManager netherManager() {
    return netherManager;
  }

  public SearchManager searchManager() {
    return searchManager;
  }

  public ScopeManager scopeManager() {
    return scopeManager;
  }

  public TunnelManager tunnelManager() {
    return tunnelManager;
  }

  public StatsManager statsManager() {
    return statsManager;
  }

}
