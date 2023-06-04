/*
 * MIT License
 *
 * Copyright (c) whimxiqal
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

import java.nio.file.Path;
import net.whimxiqal.journey.config.ConfigManager;
import net.whimxiqal.journey.data.DataManager;
import net.whimxiqal.journey.data.DataManagerImpl;
import net.whimxiqal.journey.manager.SchedulingManager;
import net.whimxiqal.journey.navigation.PlatformProxy;
import net.whimxiqal.journey.util.CommonLogger;
import net.kyori.adventure.platform.AudienceProvider;

public class ProxyImpl implements Proxy {

  private CommonLogger logger;
  private Path dataFolder;
  private AudienceProvider audienceProvider;
  private ConfigManager configManager;
  private SchedulingManager schedulingManager;
  private DataManager dataManager = new DataManagerImpl();  // default data manager
  private PlatformProxy platformProxy;
  private String version;

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

  public void dataManager(DataManager dataManager) {
    this.dataManager = dataManager;
  }

  @Override
  public DataManager dataManager() {
    return dataManager;
  }

  public void platform(PlatformProxy platformProxy) {
    this.platformProxy = platformProxy;
  }

  @Override
  public PlatformProxy platform() {
    return platformProxy;
  }

  public void version(String version) {
    this.version = version;
  }

  @Override
  public String version() {
    return version;
  }

}
