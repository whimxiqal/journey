package me.pietelite.journey.common;

import me.pietelite.journey.common.data.DataManager;
import me.pietelite.journey.common.data.DataManagerImpl;
import me.pietelite.journey.common.integration.IntegrationManager;
import me.pietelite.journey.common.manager.DebugManager;
import me.pietelite.journey.common.manager.NetherManager;
import me.pietelite.journey.common.manager.PlayerManager;
import me.pietelite.journey.common.manager.SearchManager;
import me.pietelite.journey.common.search.event.SearchDispatcher;
import me.pietelite.journey.common.search.event.SearchDispatcherImpl;
import me.pietelite.journey.common.util.CommonLogger;

public final class Journey {

  private static final Journey instance = new Journey();
  private Proxy proxy;
  private final DataManagerImpl dataManager = new DataManagerImpl();
  private final SearchDispatcherImpl searchEventDispatcher = new SearchDispatcherImpl();
  private final PlayerManager playerManager = new PlayerManager();
  private final DebugManager debugManager = new DebugManager();
  private final NetherManager netherManager = new NetherManager();
  private final SearchManager searchManager = new SearchManager();
  private final IntegrationManager integrationManager = new IntegrationManager();

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
    dataManager.init();
    netherManager.load();
    searchEventDispatcher.init();
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
