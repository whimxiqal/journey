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
import net.whimxiqal.journey.Proxy;
import net.whimxiqal.journey.config.ConfigManager;
import net.whimxiqal.journey.config.TestConfigManager;
import net.whimxiqal.journey.manager.SchedulingManager;
import net.whimxiqal.journey.manager.TestSchedulingManager;
import net.whimxiqal.journey.navigation.PlatformProxy;
import net.whimxiqal.journey.util.CommonLogger;
import net.whimxiqal.journey.util.TestAudienceProvider;
import net.whimxiqal.journey.util.TestLogger;
import net.whimxiqal.journey.platform.TestPlatformProxy;
import net.kyori.adventure.platform.AudienceProvider;

public class TestProxy implements Proxy {
  TestLogger logger = new TestLogger();
  AudienceProvider audienceProvider = new TestAudienceProvider();
  ConfigManager configManager = new TestConfigManager();
  public TestSchedulingManager schedulingManager = new TestSchedulingManager();
  PlatformProxy platformProxy = new TestPlatformProxy();

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
  public ConfigManager configManager() {
    return configManager;
  }

  @Override
  public SchedulingManager schedulingManager() {
    return schedulingManager;
  }

  @Override
  public PlatformProxy platform() {
    return platformProxy;
  }

  @Override
  public String version() {
    return "0";
  }
}
