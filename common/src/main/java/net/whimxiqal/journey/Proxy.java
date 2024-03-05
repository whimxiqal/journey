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
import net.whimxiqal.journey.data.DataManager;
import net.whimxiqal.journey.manager.SchedulingManager;
import net.whimxiqal.journey.navigation.PlatformProxy;
import net.whimxiqal.journey.proxy.AudienceProvider;
import net.whimxiqal.journey.util.CommonLogger;

public interface Proxy {

  CommonLogger logger();

  Path dataFolder();

  AudienceProvider audienceProvider();

  Path configPath();

  SchedulingManager schedulingManager();

  DataManager dataManager();

  PlatformProxy platform();

  String version();

  AssetVersion assetVersion();

  default void initialize() {
    // initialize scheduling manager first because most init/shutdown scripts need it
    schedulingManager().initialize();
    logger().initialize();
    dataManager().initialize();
  }

  default void shutdown() {
    logger().shutdown();

    // shutdown scheduling manager last because most init/shutdown scripts need it
    schedulingManager().shutdown();
  }
}
