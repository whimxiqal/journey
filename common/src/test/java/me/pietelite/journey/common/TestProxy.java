/*
 * MIT License
 *
 * Copyright 2022 Pieter Svenson
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
 *
 */

package me.pietelite.journey.common;

import me.pietelite.journey.common.config.ConfigManager;
import me.pietelite.journey.common.config.TestConfigManager;
import me.pietelite.journey.common.data.DataManager;
import me.pietelite.journey.common.data.TestDataManager;
import me.pietelite.journey.common.manager.SchedulingManager;
import me.pietelite.journey.common.manager.TestSchedulingManager;
import me.pietelite.journey.common.navigation.PlatformProxy;
import me.pietelite.journey.common.util.LoggerCommon;
import me.pietelite.journey.common.util.TestAudienceProvider;
import me.pietelite.journey.common.util.TestLogger;
import net.kyori.adventure.platform.AudienceProvider;

public class TestProxy implements Proxy {
  @Override
  public LoggerCommon logger() {
    return new TestLogger();
  }

  @Override
  public DataManager dataManager() {
    return new TestDataManager();
  }

  @Override
  public AudienceProvider audienceProvider() {
    return new TestAudienceProvider();
  }

  @Override
  public ConfigManager configManager() {
    return new TestConfigManager();
  }

  @Override
  public SchedulingManager schedulingManager() {
    return new TestSchedulingManager();
  }

  @Override
  public PlatformProxy platform() {
    return null;
  }
}
