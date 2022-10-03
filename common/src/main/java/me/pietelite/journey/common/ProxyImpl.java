package me.pietelite.journey.common;

import me.pietelite.journey.common.config.ConfigManager;
import me.pietelite.journey.common.data.DataManager;
import me.pietelite.journey.common.manager.SchedulingManager;
import me.pietelite.journey.common.manager.SearchManager;
import me.pietelite.journey.common.navigation.PlatformProxy;
import me.pietelite.journey.common.util.LoggerCommon;
import net.kyori.adventure.platform.AudienceProvider;

public class ProxyImpl implements Proxy {

  private LoggerCommon logger;
  private DataManager dataManager;
  private AudienceProvider audienceProvider;
  private ConfigManager configManager;
  private SchedulingManager schedulingManager;
  private SearchManager searchManager;
  private PlatformProxy platformProxy;

  public void logger(LoggerCommon logger) {
    this.logger = logger;
  }

  @Override
  public LoggerCommon logger() {
    return logger;
  }

  public void dataManager(DataManager dataManager) {
    this.dataManager = dataManager;
  }

  @Override
  public DataManager dataManager() {
    return dataManager;
  }

  public void audienceProvider(AudienceProvider audienceProvider) {
    this.audienceProvider = audienceProvider;
  }

  @Override
  public AudienceProvider audienceProvider() {
    return audienceProvider;
  }

  public void configManager(ConfigManager configManager) {
    this.configManager = configManager;
  }

  @Override
  public ConfigManager configManager() {
    return configManager;
  }

  public void schedulingManager(SchedulingManager schedulingManager) {
    this.schedulingManager = schedulingManager;
  }

  @Override
  public SchedulingManager schedulingManager() {
    return schedulingManager;
  }

  public void cellProxy(PlatformProxy platformProxy) {
    this.platformProxy = platformProxy;
  }

  @Override
  public PlatformProxy platform() {
    return platformProxy;
  }
}
