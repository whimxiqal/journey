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

package edu.whimc.journey.spigot;

import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.common.cache.PathCache;
import edu.whimc.journey.common.data.DataManager;
import edu.whimc.journey.common.search.event.SearchDispatcher;
import edu.whimc.journey.common.search.event.SearchEvent;
import edu.whimc.journey.spigot.command.JourneyCommand;
import edu.whimc.journey.spigot.command.NavCommand;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.config.SpigotConfigManager;
import edu.whimc.journey.spigot.data.SpigotDataManager;
import edu.whimc.journey.spigot.manager.DebugManager;
import edu.whimc.journey.spigot.manager.NetherManager;
import edu.whimc.journey.spigot.manager.PlayerSearchManager;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.search.event.SpigotFoundSolutionEvent;
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
import edu.whimc.journey.spigot.search.listener.SearchListener;
import edu.whimc.journey.spigot.util.LoggerSpigot;
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

public final class JourneySpigot extends JavaPlugin {

  private static final String SERIALIZED_PATH_CACHE_FILENAME = "paths.ser";
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

  // Database
  @Getter
  private DataManager<LocationCell, World> dataManager;

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

    // Create caches
    JourneyCommon.setLogger(new LoggerSpigot());
    JourneyCommon.setConfigManager(SpigotConfigManager.initialize("config.yml"));
    JourneyCommon.setPathCache(new PathCache<LocationCell, World>());

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
    JourneyCommon.setSearchEventDispatcher(dispatcher);

    // Set up data manager
    this.dataManager = new SpigotDataManager();

    // Register commands
    for (CommandNode root : new CommandNode[]{new JourneyCommand(), new NavCommand()}) {
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
      // TODO initialize likely-used paths here (link to link)
      valid = true;
      JourneySpigot.getInstance().getLogger().info("Finished initializing Journey");
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

      JourneyCommon.setPathCache((PathCache<LocationCell, World>) in.readObject());
      JourneySpigot.getInstance().getLogger().info("Deserialized trail cache (" + JourneyCommon.getPathCache().size() + " trails)");
      return true;

    } catch (IOException | ClassNotFoundException e) {
      JourneySpigot.getInstance().getLogger().severe("Could not deserialize trail caches!");
      return false;
    }
  }

  private boolean serializeCaches() {
    File file = Paths.get(this.getDataFolder().toPath().toString(), SERIALIZED_PATH_CACHE_FILENAME).toFile();
    try {
      //noinspection ResultOfMethodCallIgnored
      this.getDataFolder().mkdirs();
      if (file.createNewFile()) {
        JourneySpigot.getInstance().getLogger().info("Created serialized trail file");
      }
    } catch (IOException e) {
      JourneySpigot.getInstance().getLogger().severe("Could not create serialization file");
      return false;
    }

    try (FileOutputStream fileStream = new FileOutputStream(Paths.get(
        this.getDataFolder().toPath().toString(),
        SERIALIZED_PATH_CACHE_FILENAME).toFile());
         ObjectOutputStream out = new ObjectOutputStream(fileStream)) {

      out.writeObject(JourneyCommon.getPathCache());
      JourneySpigot.getInstance().getLogger().info("Serialized trail cache ("
          + JourneyCommon.getPathCache().size() + " trails)");
      return true;

    } catch (IOException e) {
      JourneySpigot.getInstance().getLogger().severe("Could not serialize trail caches");
      e.printStackTrace();
      return false;
    }
  }

}
