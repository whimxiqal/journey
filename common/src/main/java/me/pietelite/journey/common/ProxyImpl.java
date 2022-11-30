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
