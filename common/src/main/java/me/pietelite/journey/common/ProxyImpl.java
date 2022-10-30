package me.pietelite.journey.common;

import java.nio.file.Path;
import me.pietelite.journey.common.config.ConfigManager;
import me.pietelite.journey.common.manager.SchedulingManager;
import me.pietelite.journey.common.manager.SearchManager;
import me.pietelite.journey.common.navigation.PlatformProxy;
import me.pietelite.journey.common.util.CommonLogger;
import net.kyori.adventure.platform.AudienceProvider;

public class ProxyImpl implements Proxy {

  private CommonLogger logger;
  private Path dataFolder;
  private AudienceProvider audienceProvider;
  private ConfigManager configManager;
  private SchedulingManager schedulingManager;
  private SearchManager searchManager;
  private PlatformProxy platformProxy;

  public void logger(CommonLogger logger) {
    this.logger = logger;
  }

  @Override
  public CommonLogger logger() {
    return logger;
  }

  public void dataFolder(Path dataFolder) {
    this.dataFolder = dataFolder;
  }

  @Override
  public Path dataFolder() {
    return dataFolder;
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

  public void platform(PlatformProxy platformProxy) {
    this.platformProxy = platformProxy;
  }

  @Override
  public PlatformProxy platform() {
    return platformProxy;
  }


}
