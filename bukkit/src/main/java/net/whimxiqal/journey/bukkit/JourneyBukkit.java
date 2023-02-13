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

package net.whimxiqal.journey.bukkit;

import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.ProxyImpl;
import net.whimxiqal.journey.command.JourneyConnectorProvider;
import net.whimxiqal.journey.search.event.SearchDispatcher;
import net.whimxiqal.journey.search.event.SearchEvent;
import net.whimxiqal.journey.bukkit.search.event.BukkitFoundSolutionEvent;
import net.whimxiqal.journey.bukkit.search.event.BukkitIgnoreCacheSearchEvent;
import net.whimxiqal.journey.bukkit.search.event.BukkitModeFailureEvent;
import net.whimxiqal.journey.bukkit.search.event.BukkitModeSuccessEvent;
import net.whimxiqal.journey.bukkit.search.event.BukkitStartPathSearchEvent;
import net.whimxiqal.journey.bukkit.search.event.BukkitStartSearchEvent;
import net.whimxiqal.journey.bukkit.search.event.BukkitStepSearchEvent;
import net.whimxiqal.journey.bukkit.search.event.BukkitStartItinerarySearchEvent;
import net.whimxiqal.journey.bukkit.search.event.BukkitStopItinerarySearchEvent;
import net.whimxiqal.journey.bukkit.search.event.BukkitStopPathSearchEvent;
import net.whimxiqal.journey.bukkit.search.event.BukkitStopSearchEvent;
import net.whimxiqal.journey.bukkit.search.event.BukkitVisitationSearchEvent;
import net.whimxiqal.journey.bukkit.config.BukkitConfigManager;
import net.whimxiqal.journey.bukkit.listener.DeathListener;
import net.whimxiqal.journey.bukkit.listener.NetherListener;
import net.whimxiqal.journey.bukkit.search.listener.AnimationListener;
import net.whimxiqal.journey.bukkit.search.listener.DataStorageListener;
import net.whimxiqal.journey.bukkit.search.listener.PlayerSearchListener;
import net.whimxiqal.journey.bukkit.util.BukkitLogger;
import net.whimxiqal.journey.bukkit.util.BukkitSchedulingManager;
import net.whimxiqal.journey.bukkit.util.ThreadSafeBlockAccessor;
import net.whimxiqal.mantle.paper.PaperRegistrarProvider;
import net.whimxiqal.mantle.common.CommandRegistrar;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

public final class JourneyBukkit extends JavaPlugin {

  private static JourneyBukkit instance;

  private boolean valid = false;

  /**
   * Get the instance that is currently run on the Spigot server.
   *
   * @return the instance
   */
  public static JourneyBukkit get() {
    return instance;
  }

  private final ThreadSafeBlockAccessor blockAccessor = new ThreadSafeBlockAccessor();

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

    // Set up Journey Proxy
    ProxyImpl proxy = new ProxyImpl();
    Journey.get().registerProxy(proxy);
    proxy.logger(new BukkitLogger());
    proxy.dataFolder(this.getDataFolder().toPath());
    proxy.audienceProvider(new PaperAudiences(this));
    proxy.configManager(BukkitConfigManager.initialize("config.yml"));
    proxy.schedulingManager(new BukkitSchedulingManager());
    proxy.platform(new BukkitPlatformProxy());
    proxy.version(getDescription().getVersion());

    // Instantiate a SearchDispatcher. Keep registrations alphabetized
    SearchDispatcher.Editor<Event> dispatcher = Journey.get().dispatcher().editor();
    dispatcher.registerEvent(BukkitFoundSolutionEvent::new, SearchEvent.EventType.FOUND_SOLUTION);
    dispatcher.registerEvent(BukkitIgnoreCacheSearchEvent::new, SearchEvent.EventType.IGNORE_CACHE);
    dispatcher.registerEvent(BukkitModeFailureEvent::new, SearchEvent.EventType.MODE_FAILURE);
    dispatcher.registerEvent(BukkitModeSuccessEvent::new, SearchEvent.EventType.MODE_SUCCESS);
    dispatcher.registerEvent(BukkitStartItinerarySearchEvent::new, SearchEvent.EventType.START_ITINERARY);
    dispatcher.registerEvent(BukkitStartPathSearchEvent::new, SearchEvent.EventType.START_PATH);
    dispatcher.registerEvent(BukkitStartSearchEvent::new, SearchEvent.EventType.START);
    dispatcher.registerEvent(BukkitStepSearchEvent::new, SearchEvent.EventType.STEP);
    dispatcher.registerEvent(BukkitStopItinerarySearchEvent::new, SearchEvent.EventType.STOP_ITINERARY);
    dispatcher.registerEvent(BukkitStopPathSearchEvent::new, SearchEvent.EventType.STOP_PATH);
    dispatcher.registerEvent(BukkitStopSearchEvent::new, SearchEvent.EventType.STOP);
    dispatcher.registerEvent(BukkitVisitationSearchEvent::new, SearchEvent.EventType.VISITATION);
    dispatcher.setExternalDispatcher(event -> Bukkit.getPluginManager().callEvent(event));

    // Initialize common Journey (after proxy is set up)
    Journey.get().init();

    // Register command
    CommandRegistrar registrar = PaperRegistrarProvider.get(this);
    registrar.register(JourneyConnectorProvider.connector());

    Bukkit.getPluginManager().registerEvents(new NetherListener(), this);
    Bukkit.getPluginManager().registerEvents(new AnimationListener(), this);
    Bukkit.getPluginManager().registerEvents(new DataStorageListener(), this);
    Bukkit.getPluginManager().registerEvents(new PlayerSearchListener(), this);
    Bukkit.getPluginManager().registerEvents(new DeathListener(), this);

    // Initialize tasks for async capabilities
    blockAccessor.init();
    ((BukkitLogger) proxy.logger()).init();

    // Start doing a bunch of searches for common use cases
    Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
      // TODO initialize likely-used paths here (link to link)
      valid = true;
      JourneyBukkit.get().getLogger().info("Finished initializing Journey");
    });
  }

  @Override
  public void onDisable() {
    // Common Journey shutdown
    Journey.get().shutdown();

    // Plugin shutdown logic
    blockAccessor.shutdown();
  }

  public ThreadSafeBlockAccessor getBlockAccessor() {
    return blockAccessor;
  }
}
