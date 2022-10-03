package me.pietelite.journey.spigot;

import me.pietelite.journey.common.Proxy;
import me.pietelite.journey.common.config.ConfigManager;
import me.pietelite.journey.common.data.DataManager;
import me.pietelite.journey.common.manager.SchedulingManager;
import me.pietelite.journey.common.navigation.PlatformProxy;
import me.pietelite.journey.common.util.LoggerCommon;
import net.kyori.adventure.platform.AudienceProvider;

public class SpigotProxy implements Proxy {
  @Override
  public LoggerCommon logger() {
    return null;
  }

  @Override
  public DataManager dataManager() {
    return null;
  }

  @Override
  public AudienceProvider audienceProvider() {
    return null;
  }

  @Override
  public ConfigManager configManager() {
    return null;
  }

  @Override
  public SchedulingManager schedulingManager() {
    return null;
  }

  @Override
  public PlatformProxy platform() {
    return null;
  }
}
