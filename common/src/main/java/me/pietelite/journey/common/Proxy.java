package me.pietelite.journey.common;

import me.pietelite.journey.common.config.ConfigManager;
import me.pietelite.journey.common.data.DataManager;
import me.pietelite.journey.common.manager.SchedulingManager;
import me.pietelite.journey.common.navigation.PlatformProxy;
import me.pietelite.journey.common.util.LoggerCommon;
import net.kyori.adventure.platform.AudienceProvider;

public interface Proxy {

  LoggerCommon logger();

  DataManager dataManager();

  AudienceProvider audienceProvider();

  ConfigManager configManager();

  SchedulingManager schedulingManager();

  PlatformProxy platform();

}
