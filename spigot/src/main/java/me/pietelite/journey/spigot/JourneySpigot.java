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

package me.pietelite.journey.spigot;

import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.ProxyImpl;
import me.pietelite.journey.common.command.JourneyConnectorProvider;
import me.pietelite.journey.common.integration.Integrator;
import me.pietelite.journey.common.integration.PotentialIntegrator;
import me.pietelite.journey.common.search.event.SearchDispatcher;
import me.pietelite.journey.common.search.event.SearchEvent;
import me.pietelite.journey.spigot.integration.PotentialEssentialsIntegrator;
import me.pietelite.journey.spigot.search.event.SpigotFoundSolutionEvent;
import me.pietelite.journey.spigot.search.event.SpigotIgnoreCacheSearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotModeFailureEvent;
import me.pietelite.journey.spigot.search.event.SpigotModeSuccessEvent;
import me.pietelite.journey.spigot.search.event.SpigotStartItinerarySearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotStartPathSearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotStartSearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotStepSearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotStopItinerarySearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotStopPathSearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotStopSearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotVisitationSearchEvent;
import me.pietelite.journey.spigot.config.SpigotConfigManager;
import me.pietelite.journey.spigot.listener.DeathListener;
import me.pietelite.journey.spigot.listener.NetherListener;
import me.pietelite.journey.spigot.search.listener.AnimationListener;
import me.pietelite.journey.spigot.search.listener.DataStorageListener;
import me.pietelite.journey.spigot.search.listener.PlayerSearchListener;
import me.pietelite.journey.spigot.util.SpigotLogger;
import me.pietelite.journey.spigot.util.SpigotSchedulingManager;
import me.pietelite.journey.spigot.util.ThreadSafeBlockAccessor;
import me.pietelite.mantle.bukkit.BukkitRegistrarProvider;
import me.pietelite.mantle.common.CommandRegistrar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

public final class JourneySpigot extends JavaPlugin {

  private static JourneySpigot instance;

  private boolean valid = false;

  /**
   * Get the instance that is currently run on the Spigot server.
   *
   * @return the instance
   */
  public static JourneySpigot getInstance() {
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

    // Set up Journey Common
    ProxyImpl proxy = new ProxyImpl();
    Journey.get().registerProxy(proxy);
    proxy.logger(new SpigotLogger());
    proxy.dataFolder(this.getDataFolder().toPath());
    proxy.audienceProvider(BukkitAudiences.create(this));
    proxy.configManager(SpigotConfigManager.initialize("config.yml"));
    proxy.schedulingManager(new SpigotSchedulingManager());
    proxy.platform(new SpigotPlatformProxy());

    // Instantiate a SearchDispatcher. Keep registrations alphabetized
    SearchDispatcher.Editor<Event> dispatcher = Journey.get().dispatcher().editor();
    dispatcher.registerEvent(SpigotFoundSolutionEvent::new, SearchEvent.EventType.FOUND_SOLUTION);
    dispatcher.registerEvent(SpigotIgnoreCacheSearchEvent::new, SearchEvent.EventType.IGNORE_CACHE);
    dispatcher.registerEvent(SpigotModeFailureEvent::new, SearchEvent.EventType.MODE_FAILURE);
    dispatcher.registerEvent(SpigotModeSuccessEvent::new, SearchEvent.EventType.MODE_SUCCESS);
    dispatcher.registerEvent(SpigotStartItinerarySearchEvent::new, SearchEvent.EventType.START_ITINERARY);
    dispatcher.registerEvent(SpigotStartPathSearchEvent::new, SearchEvent.EventType.START_PATH);
    dispatcher.registerEvent(SpigotStartSearchEvent::new, SearchEvent.EventType.START);
    dispatcher.registerEvent(SpigotStepSearchEvent::new, SearchEvent.EventType.STEP);
    dispatcher.registerEvent(SpigotStopItinerarySearchEvent::new, SearchEvent.EventType.STOP_ITINERARY);
    dispatcher.registerEvent(SpigotStopPathSearchEvent::new, SearchEvent.EventType.STOP_PATH);
    dispatcher.registerEvent(SpigotStopSearchEvent::new, SearchEvent.EventType.STOP);
    dispatcher.registerEvent(SpigotVisitationSearchEvent::new, SearchEvent.EventType.VISITATION);
    dispatcher.setExternalDispatcher(event -> Bukkit.getPluginManager().callEvent(event));

    // Initialize common Journey (after proxy is set up)
    Journey.get().init();

    // Set up data manager

    // Register command
//    CommandNode root = new JourneyCommand();
//    PluginCommand command = getCommand(root.getPrimaryAlias());
//    if (command == null) {
//      throw new NullPointerException("You must register command "
//          + root.getPrimaryAlias()
//          + " in the plugin.yml");
//    }
//    command.setExecutor(root);
//    command.setTabCompleter(root);
//    root.getPermission().map(Permission::getName).ifPresent(command::setPermission);
    CommandRegistrar registrar = BukkitRegistrarProvider.get(this);
    registrar.register(JourneyConnectorProvider.connector());

    Bukkit.getPluginManager().registerEvents(new NetherListener(), this);
    Bukkit.getPluginManager().registerEvents(new AnimationListener(), this);
    Bukkit.getPluginManager().registerEvents(new DataStorageListener(), this);
    Bukkit.getPluginManager().registerEvents(new PlayerSearchListener(), this);
    Bukkit.getPluginManager().registerEvents(new DeathListener(), this);

    // Initialize tasks for async capabilities
    blockAccessor.init();
    ((SpigotLogger) proxy.logger()).init();

    // Start doing a bunch of searches for common use cases
    Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
      // TODO initialize likely-used paths here (link to link)
      valid = true;
      JourneySpigot.getInstance().getLogger().info("Finished initializing Journey");
    });

    // bStats
    Metrics metrics = new Metrics(this, 14192);

    // Integrators
    attemptIntegrator(new PotentialEssentialsIntegrator());
  }

  private void attemptIntegrator(PotentialIntegrator<?> potential) {
    if (potential.viable()) {
      Integrator integrator = potential.integrator();
      Journey.get().integrationManager().register(integrator);
      Journey.logger().info("Added integrator: " + integrator.name());
    } else {
      Journey.logger().info("Did not add integrator");
    }
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
