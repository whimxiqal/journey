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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import net.whimxiqal.journey.data.DataManager;
import net.whimxiqal.journey.data.TestDataManager;
import net.whimxiqal.journey.manager.SchedulingManager;
import net.whimxiqal.journey.manager.TestSchedulingManager;
import net.whimxiqal.journey.navigation.PlatformProxy;
import net.whimxiqal.journey.proxy.AudienceProvider;
import net.whimxiqal.journey.util.CommonLogger;
import net.whimxiqal.journey.util.TestAudienceProvider;
import net.whimxiqal.journey.util.TestLogger;

public class TestProxy implements Proxy {

  final Path configPath;
  TestLogger logger = new TestLogger();
  AudienceProvider audienceProvider = new TestAudienceProvider();
  TestSchedulingManager schedulingManager = new TestSchedulingManager();
  DataManager dataManager = new TestDataManager();
  PlatformProxy platformProxy;

  public TestProxy(PlatformProxy platformProxy) {
    this.platformProxy = platformProxy;

    try {
      configPath = File.createTempFile("journey-config", "yml").toPath();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public CommonLogger logger() {
    return logger;
  }

  @Override
  public Path dataFolder() {
    return null;
  }

  @Override
  public AudienceProvider audienceProvider() {
    return audienceProvider;
  }

  @Override
  public Path configPath() {
    return configPath;
  }

  @Override
  public SchedulingManager schedulingManager() {
    return schedulingManager;
  }

  @Override
  public DataManager dataManager() {
    return dataManager;
  }

  @Override
  public PlatformProxy platform() {
    return platformProxy;
  }

  @Override
  public String version() {
    return "0";
  }

  @Override
  public AssetVersion assetVersion() {
    return AssetVersion.MINECRAFT_17;
  }

}
