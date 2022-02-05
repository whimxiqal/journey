/*
 * MIT License
 *
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.journey.spigot;

import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.common.ml.ScoringNetwork;
import edu.whimc.journey.common.search.event.SearchDispatcher;
import edu.whimc.journey.common.search.event.SearchEvent;
import edu.whimc.journey.common.util.Serialize;
import edu.whimc.journey.spigot.command.JourneyCommand;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.config.SpigotConfigManager;
import edu.whimc.journey.spigot.data.SpigotDataManager;
import edu.whimc.journey.spigot.manager.DebugManager;
import edu.whimc.journey.spigot.manager.NetherManager;
import edu.whimc.journey.spigot.manager.PlayerSearchManager;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.search.event.SpigotFoundSolutionEvent;
import edu.whimc.journey.spigot.search.event.SpigotIgnoreCacheSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotModeFailureEvent;
import edu.whimc.journey.spigot.search.event.SpigotModeSuccessEvent;
import edu.whimc.journey.spigot.search.event.SpigotStartItinerarySearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStartPathSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStartSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStepSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStopItinerarySearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStopPathSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStopSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotVisitationSearchEvent;
import edu.whimc.journey.spigot.search.listener.AnimationListener;
import edu.whimc.journey.spigot.search.listener.DataStorageListener;
import edu.whimc.journey.spigot.search.listener.PlayerSearchListener;
import edu.whimc.journey.spigot.util.LoggerSpigot;
import edu.whimc.journey.spigot.util.SpigotMinecraftConversions;
import java.util.Random;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The base plugin class of the Spigot implementation of Journey.
 * Used in tandem with {@link JourneyCommon} throughout the plugin.
 *
 * @see JourneyCommon
 */
public final class JourneySpigot extends JavaPlugin {

  private static JourneySpigot instance;

  // Caches
  @Getter
  private NetherManager netherManager;
  @Getter
  private DebugManager debugManager;
  @Getter
  private PlayerSearchManager searchManager;
  @Getter
  private boolean valid = false;

  /**
   * Get the instance that is currently run on the Spigot server.
   *
   * @return the instance
   */
  public static JourneySpigot getInstance() {
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

    // Set up Journey Common
    JourneyCommon.setLogger(new LoggerSpigot());
    JourneyCommon.setConfigManager(SpigotConfigManager.initialize("config.yml"));
    JourneyCommon.setConversions(new SpigotMinecraftConversions());

    // Set up caches for Spigot Journey
    this.netherManager = new NetherManager();
    this.debugManager = new DebugManager();
    this.searchManager = new PlayerSearchManager();

    deserializeCaches();

    // Instantiate a SearchDispatcher. Keep registrations alphabetized
    SearchDispatcher<LocationCell, World, Event> dispatcher = new SearchDispatcher<>(event ->
        Bukkit.getServer().getPluginManager().callEvent(event));
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
    JourneyCommon.setSearchEventDispatcher(dispatcher);

    // Set up data manager
    JourneyCommon.setDataManager(new SpigotDataManager());

    // Register command
    CommandNode root = new JourneyCommand();
    PluginCommand command = getCommand(root.getPrimaryAlias());
    if (command == null) {
      throw new NullPointerException("You must register command "
          + root.getPrimaryAlias()
          + " in the plugin.yml");
    }
    command.setExecutor(root);
    command.setTabCompleter(root);
    root.getPermission().map(Permission::getName).ifPresent(command::setPermission);

    // Register listeners
    Bukkit.getPluginManager().registerEvents(netherManager, this);
    Bukkit.getPluginManager().registerEvents(new AnimationListener(), this);
    Bukkit.getPluginManager().registerEvents(new DataStorageListener(), this);
    Bukkit.getPluginManager().registerEvents(new PlayerSearchListener(), this);


    // Start doing a bunch of searches for common use cases
    Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
      // TODO initialize likely-used paths here (link to link)
      valid = true;
      JourneySpigot.getInstance().getLogger().info("Finished initializing Journey");
    });

    // TODO make these parameters configurable in the config settings
    int TRAINING_DATA_COUNT = 20000;
    Random random = new Random(12345);
    Bukkit.getScheduler().runTaskTimerAsynchronously(this,
        () -> {
          if (JourneyCommon.getDataManager().getPathRecordManager().hasAtLeastCellCount(TRAINING_DATA_COUNT)) {
            getLogger().info("Training journey network");
            getLogger().info("Current network error: " + JourneyCommon.getNetwork().getError());
            Bukkit.getScheduler().scheduleSyncDelayedTask(this,
                () -> JourneyCommon.getNetwork().stopLearning(),
                20 * 60 * 15 /* 15 minutes */);
            JourneyCommon.getNetwork().learn(
                JourneyCommon.getDataManager()
                    .getPathRecordManager()
                    .getRandomTrainingCells(random, TRAINING_DATA_COUNT));
            getLogger().info("Stopped training Journey network");
            getLogger().info("Current network error: " + JourneyCommon.getNetwork().getError());
          } else {
            getLogger().info("Tried to train Journey network, but did not find enough data");
          }

        },
        0 /* wait 0 seconds */,
        20 * 60 * 60 /* wait 1 hour in between training */);

  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getSearchManager().cancelAllSearches();
    getSearchManager().stopAllJourneys();
    serializeCaches();
  }

  private void deserializeCaches() {

    // Nether Ports cache
    Serialize.deserializeCache(this.getDataFolder(),
        NetherManager.NETHER_MANAGER_CACHE_FILE_NAME,
        manager -> this.netherManager = manager,
        NetherManager::new);
    JourneySpigot.getInstance().getLogger().info(this.netherManager.size() + " nether ports deserialized");

    // Neutral Network cache
    Serialize.deserializeCache(this.getDataFolder(),
        ScoringNetwork.CACHE_FILE_NAME,
        JourneyCommon::setNetwork,
        ScoringNetwork::new);
    JourneySpigot.getInstance().getLogger().info("Neural Network deserialized");
  }

  private void serializeCaches() {

    // nether Ports cache
    Serialize.serializeCache(this.getDataFolder(),
        NetherManager.NETHER_MANAGER_CACHE_FILE_NAME,
        () -> this.netherManager,
        manager -> this.netherManager = manager,
        NetherManager::new);
    JourneySpigot.getInstance().getLogger().info(this.netherManager.size() + " nether ports serialized");

    // Neural Network cache
    Serialize.serializeCache(this.getDataFolder(),
        ScoringNetwork.CACHE_FILE_NAME,
        JourneyCommon::getNetwork,
        JourneyCommon::setNetwork,
        ScoringNetwork::new);
    JourneySpigot.getInstance().getLogger().info("Neural Network serialized");
  }

}
