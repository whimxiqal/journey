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

package net.whimxiqal.journey.integrations.multiverse;

import com.onarandombox.MultiverseCore.MultiverseCore;
import java.util.logging.Logger;
import net.whimxiqal.journey.JourneyApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class JourneyMultiverse extends JavaPlugin {

  private static JourneyMultiverse instance;
  private static Logger logger;

  public static JourneyMultiverse instance() {
    return instance;
  }

  public static Logger logger() {
    return logger;
  }

  @Override
  public void onEnable() {
    JourneyMultiverse.instance = this;
    JourneyMultiverse.logger = getLogger();

    Plugin journey = getServer().getPluginManager().getPlugin("Journey");
    if (journey == null) {
      logger().severe("Could not find Journey");
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }


    JourneyApi.get().registerScope(getName(), "multiverse", new MultiverseScope());

  }

}