/*
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
 */

package edu.whimc.indicator.spigot;

import edu.whimc.indicator.common.IndicatorCommon;
import edu.whimc.indicator.common.cache.PathCache;
import edu.whimc.indicator.common.data.DataManager;
import edu.whimc.indicator.common.manager.SearchManager;
import edu.whimc.indicator.common.search.event.SearchDispatcher;
import edu.whimc.indicator.common.search.event.SearchEvent;
import edu.whimc.indicator.spigot.command.IndicatorCommand;
import edu.whimc.indicator.spigot.command.NavCommand;
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.config.SpigotConfigManager;
import edu.whimc.indicator.spigot.data.SpigotDataManager;
import edu.whimc.indicator.spigot.manager.DebugManager;
import edu.whimc.indicator.spigot.manager.NetherManager;
import edu.whimc.indicator.spigot.manager.PlayerSearchManager;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import edu.whimc.indicator.spigot.search.event.SpigotFoundSolutionEvent;
import edu.whimc.indicator.spigot.search.event.SpigotModeFailureEvent;
import edu.whimc.indicator.spigot.search.event.SpigotModeSuccessEvent;
import edu.whimc.indicator.spigot.search.event.SpigotStartItinerarySearchEvent;
import edu.whimc.indicator.spigot.search.event.SpigotStartPathSearchEvent;
import edu.whimc.indicator.spigot.search.event.SpigotStartSearchEvent;
import edu.whimc.indicator.spigot.search.event.SpigotStepSearchEvent;
import edu.whimc.indicator.spigot.search.event.SpigotStopItinerarySearchEvent;
import edu.whimc.indicator.spigot.search.event.SpigotStopPathSearchEvent;
import edu.whimc.indicator.spigot.search.event.SpigotStopSearchEvent;
import edu.whimc.indicator.spigot.search.event.SpigotVisitationSearchEvent;
import edu.whimc.indicator.spigot.search.listener.AnimationListener;
import edu.whimc.indicator.spigot.search.listener.DataStorageListener;
import edu.whimc.indicator.spigot.search.listener.SearchListener;
import edu.whimc.indicator.spigot.util.LoggerSpigot;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

public final class IndicatorSpigot extends JavaPlugin {

  private static final String SERIALIZED_PATH_CACHE_FILENAME = "paths.ser";
  private static IndicatorSpigot instance;
  // Caches
  @Getter
  private NetherManager netherManager;
  @Getter
  private DebugManager debugManager;
  @Getter
  private PlayerSearchManager searchManager;
  @Getter
  private boolean valid = false;

  // Database
  @Getter
  private DataManager<LocationCell, World> dataManager;

  public static IndicatorSpigot getInstance() {
    return instance;
  }

  @Override
  public void onLoad() {
    instance = this;
  }

  @Override
  public void onEnable() {
    getLogger().info("Initializing Indicator...");

    if (this.getDataFolder().mkdirs()) {
      getLogger().info("Indicator data folder created.");
    }

    // Create caches
    IndicatorCommon.setLogger(new LoggerSpigot());
    IndicatorCommon.setConfigManager(SpigotConfigManager.initialize("config.yml"));
    IndicatorCommon.setPathCache(new PathCache<LocationCell, World>());

    this.netherManager = new NetherManager();
    this.debugManager = new DebugManager();
    this.searchManager = new PlayerSearchManager();

    deserializeCaches();

    // Instantiate a SearchDispatcher. Keep registrations alphabetized
    SearchDispatcher<LocationCell, World, Event> dispatcher = new SearchDispatcher<>(event ->
        Bukkit.getServer().getPluginManager().callEvent(event));
    dispatcher.registerEvent(SpigotFoundSolutionEvent::new, SearchEvent.EventType.FOUND_SOLUTION);
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
    IndicatorCommon.setSearchEventDispatcher(dispatcher);

    // Set up data manager
    this.dataManager = new SpigotDataManager();

    // Register commands
    for (CommandNode root : new CommandNode[]{new IndicatorCommand(), new NavCommand()}) {
      PluginCommand command = getCommand(root.getPrimaryAlias());
      if (command == null) {
        throw new NullPointerException("You must register command " + root.getPrimaryAlias() + " in the plugin.yml");
      }
      command.setExecutor(root);
      command.setTabCompleter(root);
      root.getPermission().map(Permission::getName).ifPresent(command::setPermission);
    }
    // Register listeners
    Bukkit.getPluginManager().registerEvents(netherManager, this);
    Bukkit.getPluginManager().registerEvents(new AnimationListener(), this);
    Bukkit.getPluginManager().registerEvents(new DataStorageListener(), this);
    Bukkit.getPluginManager().registerEvents(new SearchListener(), this);


    // Start doing a bunch of searches for common use cases
    Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
//      initializeTrails();
      valid = true;
      IndicatorSpigot.getInstance().getLogger().info("Finished initializing Indicator");
    });
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getSearchManager().stopAllJourneys();
    serializeCaches();
  }

  @SuppressWarnings("unchecked")
  private boolean deserializeCaches() {
    File file = Paths.get(this.getDataFolder().toPath().toString(), SERIALIZED_PATH_CACHE_FILENAME).toFile();
    if (!file.exists()) return false;
    try (FileInputStream fileStream = new FileInputStream(file);
         ObjectInputStream in = new ObjectInputStream(fileStream)) {

      IndicatorCommon.setPathCache((PathCache<LocationCell, World>) in.readObject());
      IndicatorSpigot.getInstance().getLogger().info("Deserialized trail cache (" + IndicatorCommon.getPathCache().size() + " trails)");
      return true;

    } catch (IOException | ClassNotFoundException e) {
      IndicatorSpigot.getInstance().getLogger().severe("Could not deserialize trail caches!");
      return false;
    }
  }

  private boolean serializeCaches() {
    File file = Paths.get(this.getDataFolder().toPath().toString(), SERIALIZED_PATH_CACHE_FILENAME).toFile();
    try {
      //noinspection ResultOfMethodCallIgnored
      this.getDataFolder().mkdirs();
      if (file.createNewFile()) {
        IndicatorSpigot.getInstance().getLogger().info("Created serialized trail file");
      }
    } catch (IOException e) {
      IndicatorSpigot.getInstance().getLogger().severe("Could not create serialization file");
      return false;
    }

    try (FileOutputStream fileStream = new FileOutputStream(Paths.get(
        this.getDataFolder().toPath().toString(),
        SERIALIZED_PATH_CACHE_FILENAME).toFile());
         ObjectOutputStream out = new ObjectOutputStream(fileStream)) {

      out.writeObject(IndicatorCommon.getPathCache());
      IndicatorSpigot.getInstance().getLogger().info("Serialized trail cache ("
          + IndicatorCommon.getPathCache().size() + " trails)");
      return true;

    } catch (IOException e) {
      IndicatorSpigot.getInstance().getLogger().severe("Could not serialize trail caches");
      e.printStackTrace();
      return false;
    }
  }

  // TODO maybe create another method like this that will initialize "cacheable" paths,
  //  which are just paths that go between locations that are unlikely to move and likely
  //  to be used many times, like Leap to Leap.
//  private void initializeTrails() {
//    SearchTracker<LocationCell, World> tracker = new SpigotSearchTracker();
//
//    // Cache survival modes
//    OldIndicatorSearch search = new OldIndicatorSearch(OldIndicatorSearch.SURVIVAL_MODES, s -> true);
//    search.setTracker(tracker);
//    search.searchCacheable();
//
//    // Cache creative modes (flying)
//    search = new OldIndicatorSearch(Collections.singleton(new FlyMode()), s -> true);
//    search.setTracker(tracker);
//    search.searchCacheable();
//  }
}
