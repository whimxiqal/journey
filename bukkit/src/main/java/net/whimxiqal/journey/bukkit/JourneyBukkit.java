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

package net.whimxiqal.journey.bukkit;

import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.ProxyImpl;
import net.whimxiqal.journey.command.JourneyConnectorProvider;
import net.whimxiqal.journey.bukkit.config.BukkitConfigManager;
import net.whimxiqal.journey.bukkit.listener.DeathListener;
import net.whimxiqal.journey.bukkit.listener.NetherListener;
import net.whimxiqal.journey.bukkit.search.listener.PlayerListener;
import net.whimxiqal.journey.bukkit.util.BukkitLogger;
import net.whimxiqal.journey.bukkit.util.BukkitSchedulingManager;
import net.whimxiqal.mantle.paper.PaperRegistrarProvider;
import net.whimxiqal.mantle.common.CommandRegistrar;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class JourneyBukkit extends JavaPlugin {

  private static JourneyBukkit instance;

  /**
   * Get the instance that is currently run on the Spigot server.
   *
   * @return the instance
   */
  public static JourneyBukkit get() {
    return instance;
  }

  @Override
  public void onLoad() {
    instance = this;
  }

  @Override
  public void onEnable() {
    getLogger().info("Initializing Journey...");

    if (this.getDataFolder().mkdirs()) {
      getLogger().info("Journey data folder created");
    }

    // API
    JourneyBukkitApiSupplier.set(new JourneyBukkitApiImpl());

    Journey.create();
    // Set up Journey Proxy
    ProxyImpl proxy = new ProxyImpl();
    Journey.get().registerProxy(proxy);
    proxy.logger(new BukkitLogger());
    proxy.dataFolder(this.getDataFolder().toPath());
    proxy.audienceProvider(new PaperAudiences());
    proxy.configManager(BukkitConfigManager.initialize("config.yml"));
    proxy.schedulingManager(new BukkitSchedulingManager());
    proxy.platform(new BukkitPlatformProxy());
    proxy.version(getDescription().getVersion());

    // Initialize common Journey (after proxy is set up)
    if (!Journey.get().init()) {
      setEnabled(false);
    }

    // Register command
    CommandRegistrar registrar = PaperRegistrarProvider.get(this);
    registrar.register(JourneyConnectorProvider.connector());

    Bukkit.getPluginManager().registerEvents(new NetherListener(), this);
    Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
  }

  @Override
  public void onDisable() {
    // Common Journey shutdown
    Journey.get().shutdown();
    Journey.remove();
  }
}
