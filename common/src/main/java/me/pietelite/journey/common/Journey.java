package me.pietelite.journey.common;

import me.pietelite.journey.common.manager.DeathManager;
import me.pietelite.journey.common.manager.DebugManager;
import me.pietelite.journey.common.manager.NetherManager;
import me.pietelite.journey.common.manager.SearchManager;
import me.pietelite.journey.common.search.event.SearchDispatcher;

public class Journey {

  private static final Journey instance = new Journey();
  private Proxy proxy;
  private final SearchDispatcher searchEventDispatcher = new SearchDispatcher();
  private final DeathManager deathManager = new DeathManager();
  private final DebugManager debugManager = new DebugManager();
  private final NetherManager netherManager = new NetherManager();
  private final SearchManager searchManager = new SearchManager();

  public static Journey get() {
    return instance;
  }

  public Proxy proxy() {
    return proxy;
  }

  public void registerProxy(Proxy proxy) {
    if (proxy != null) {
      throw new IllegalStateException("The proxy was already registered.");
    }
    this.proxy = proxy;
  }

  public SearchDispatcher dispatcher() {
    return searchEventDispatcher;
  }

  public DeathManager deathManager() {
    return deathManager;
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
}
