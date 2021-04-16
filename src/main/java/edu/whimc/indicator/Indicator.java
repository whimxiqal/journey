package edu.whimc.indicator;

import com.google.common.collect.Lists;
import edu.whimc.indicator.common.cache.TrailCache;
import edu.whimc.indicator.common.path.*;
import edu.whimc.indicator.spigot.cache.DebugManager;
import edu.whimc.indicator.spigot.cache.JourneyManager;
import edu.whimc.indicator.spigot.cache.NetherManager;
import edu.whimc.indicator.spigot.command.EndpointCommand;
import edu.whimc.indicator.spigot.command.IndicatorCommand;
import edu.whimc.indicator.spigot.command.TrailCommand;
import edu.whimc.indicator.spigot.cache.EndpointManager;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.path.mode.*;
import edu.whimc.indicator.spigot.search.IndicatorSearch;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public final class Indicator extends JavaPlugin {

  private static Indicator instance;

  private static final String SERIALIZED_TRAIL_CACHE_FILENAME = "trails.ser";

  // Caches
  @Getter
  private EndpointManager endpointManager;
  @Getter
  private NetherManager netherManager;
  @Getter
  private DebugManager debugManager;
  @Getter
  private JourneyManager journeyManager;
  @Getter
  private TrailCache<LocationCell, World> trailCache;
  @Getter
  private boolean valid = false;

  public static Indicator getInstance() {
    return instance;
  }

  @Override
  public void onLoad() {
    instance = this;
  }

  @Override
  public void onEnable() {
    Indicator.getInstance().getLogger().info("Initializing Indicator...");
    // Create caches
    this.endpointManager = new EndpointManager();
    this.netherManager = new NetherManager();
    this.debugManager = new DebugManager();
    this.journeyManager = new JourneyManager();
    this.trailCache = new TrailCache<>();

    deserializeCaches();

    // Register commands
    Lists.newArrayList(new IndicatorCommand(),
        new TrailCommand(),
        new EndpointCommand()).forEach(root -> {
      PluginCommand command = getCommand(root.getPrimaryAlias());
      if (command == null) {
        throw new NullPointerException("You must register this command in the plugin.yml");
      }
      command.setExecutor(root);
      command.setTabCompleter(root);
      root.getPermission().map(Permission::getName).ifPresent(command::setPermission);
    });

    // Register listeners
    netherManager.registerListeners(this);
    journeyManager.registerListeners(this);

    // Start doing a bunch of searches for common use cases
    Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
      initializeTrails();
      valid = true;
      Indicator.getInstance().getLogger().info("Finished initializing Indicator");
    });
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    serializeCaches();
  }

  @SuppressWarnings("unchecked")
  private boolean deserializeCaches() {
    File file = Paths.get(this.getDataFolder().toPath().toString(), SERIALIZED_TRAIL_CACHE_FILENAME).toFile();
    if (!file.exists()) return false;
    try (FileInputStream fileStream = new FileInputStream(file);
         ObjectInputStream in = new ObjectInputStream(fileStream)) {

      trailCache = (TrailCache<LocationCell, World>) in.readObject();
      Indicator.getInstance().getLogger().info("Deserialized trail cache (" + trailCache.size() + " trails)");
      return true;

    } catch (IOException | ClassNotFoundException e) {
      Indicator.getInstance().getLogger().severe("Could not deserialize trail caches");
      e.printStackTrace();
      return false;
    }
  }

  private boolean serializeCaches() {
    File file = Paths.get(this.getDataFolder().toPath().toString(), SERIALIZED_TRAIL_CACHE_FILENAME).toFile();
    try {
      this.getDataFolder().mkdirs();
      if (file.createNewFile()) {
        Indicator.getInstance().getLogger().info("Created serialized trail file");
      }
    } catch (IOException e) {
      Indicator.getInstance().getLogger().severe("Could not create serialization file");
      return false;
    }

    try (FileOutputStream fileStream = new FileOutputStream(Paths.get(
        this.getDataFolder().toPath().toString(),
        SERIALIZED_TRAIL_CACHE_FILENAME).toFile());
         ObjectOutputStream out = new ObjectOutputStream(fileStream)) {

      out.writeObject(trailCache);
      Indicator.getInstance().getLogger().info("Serialized trail cache (" + trailCache.size() + " trails)");
      return true;

    } catch (IOException e) {
      Indicator.getInstance().getLogger().severe("Could not serialize trail caches");
      e.printStackTrace();
      return false;
    }
  }

  private void initializeTrails() {
    // Survival
    new IndicatorSearch(IndicatorSearch.SURVIVAL_MODES, s -> true).searchCacheable();
    // Creative/Flying
    new IndicatorSearch(Collections.singleton(new FlyMode()), s -> true).searchCacheable();
  }
}
