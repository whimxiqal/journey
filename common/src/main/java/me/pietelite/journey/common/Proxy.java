package me.pietelite.journey.common;

import java.nio.file.Path;
import me.pietelite.journey.common.config.ConfigManager;
import me.pietelite.journey.common.manager.SchedulingManager;
import me.pietelite.journey.common.navigation.PlatformProxy;
import me.pietelite.journey.common.util.CommonLogger;
import net.kyori.adventure.platform.AudienceProvider;

public interface Proxy {

  CommonLogger logger();

  Path dataFolder();

  AudienceProvider audienceProvider();

  ConfigManager configManager();

  SchedulingManager schedulingManager();

  PlatformProxy platform();

}
