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

package net.whimxiqal.journey.common;

import net.whimxiqal.journey.common.data.DataManager;
import net.whimxiqal.journey.common.data.DataManagerImpl;
import net.whimxiqal.journey.common.data.integration.IntegrationManager;
import net.whimxiqal.journey.common.manager.DebugManager;
import net.whimxiqal.journey.common.manager.NetherManager;
import net.whimxiqal.journey.common.manager.PlayerManager;
import net.whimxiqal.journey.common.manager.SearchManager;
import net.whimxiqal.journey.common.search.event.SearchDispatcher;
import net.whimxiqal.journey.common.search.event.SearchDispatcherImpl;
import net.whimxiqal.journey.common.util.CommonLogger;
import net.whimxiqal.journey.common.util.Initializable;

public final class Journey {

  private static final Journey instance = new Journey();
  private final SearchDispatcherImpl searchEventDispatcher = new SearchDispatcherImpl();
  private final PlayerManager playerManager = new PlayerManager();
  private final DebugManager debugManager = new DebugManager();
  private final NetherManager netherManager = new NetherManager();
  private final SearchManager searchManager = new SearchManager();
  private final IntegrationManager integrationManager = new IntegrationManager();
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
    if (dataManager instanceof Initializable) {
      ((Initializable) dataManager).initialize();
    }
    netherManager.load();
    searchEventDispatcher.initialize();
  }

  public void shutdown() {
    searchEventDispatcher.shutdown();
    searchManager.cancelAllSearches();
    searchManager.stopAllJourneys();
    proxy.audienceProvider().close();
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

  public IntegrationManager integrationManager() {
    return integrationManager;
  }

}
