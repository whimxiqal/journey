package edu.whimc.indicator;

import edu.whimc.indicator.common.cache.TrailCache;
import edu.whimc.indicator.common.config.ConfigManager;
import edu.whimc.indicator.common.data.DataManager;
import edu.whimc.indicator.common.search.tracker.SearchTracker;
import edu.whimc.indicator.spigot.cache.DebugManager;
import edu.whimc.indicator.spigot.cache.NetherManager;
import edu.whimc.indicator.spigot.cache.SearchManager;
import edu.whimc.indicator.spigot.command.IndicatorCommand;
import edu.whimc.indicator.spigot.command.TrailCommand;
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.data.SpigotDataManager;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import edu.whimc.indicator.spigot.navigation.mode.FlyMode;
import edu.whimc.indicator.spigot.search.IndicatorSearch;
import edu.whimc.indicator.spigot.search.tracker.SpigotSearchTracker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.Collections;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

public final class Indicator extends JavaPlugin {

  private static final String SERIALIZED_PATH_CACHE_FILENAME = "paths.ser";
  private static Indicator instance;
  // Caches
  @Getter
  private ConfigManager configManager;
  @Getter
  private NetherManager netherManager;
  @Getter
  private DebugManager debugManager;
  @Getter
  private SearchManager searchManager;
  @Getter
  private TrailCache<LocationCell, World> trailCache;
  @Getter
  private boolean valid = false;

  // Database
  @Getter
  private DataManager<LocationCell, World> dataManager;

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
    this.configManager = new ConfigManager("config.yml");
    this.netherManager = new NetherManager();
    this.debugManager = new DebugManager();
    this.searchManager = new SearchManager();
    this.trailCache = new TrailCache<>();

    deserializeCaches();

    // Set up data manager
    this.dataManager = new SpigotDataManager();

    // Register commands
    for (CommandNode root : new CommandNode[]{new IndicatorCommand(), new TrailCommand()}) {
      PluginCommand command = getCommand(root.getPrimaryAlias());
      if (command == null) {
        throw new NullPointerException("You must register command " + root.getPrimaryAlias() + " in the plugin.yml");
      }
      command.setExecutor(root);
      command.setTabCompleter(root);
      root.getPermission().map(Permission::getName).ifPresent(command::setPermission);
    }
    // Register listeners
    netherManager.registerListeners(this);
    searchManager.registerListeners(this);

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
    File file = Paths.get(this.getDataFolder().toPath().toString(), SERIALIZED_PATH_CACHE_FILENAME).toFile();
    if (!file.exists()) return false;
    try (FileInputStream fileStream = new FileInputStream(file);
         ObjectInputStream in = new ObjectInputStream(fileStream)) {

      trailCache = (TrailCache<LocationCell, World>) in.readObject();
      Indicator.getInstance().getLogger().info("Deserialized trail cache (" + trailCache.size() + " trails)");
      return true;

    } catch (IOException | ClassNotFoundException e) {
      Indicator.getInstance().getLogger().severe("Could not deserialize trail caches!");
      return false;
    }
  }

  private boolean serializeCaches() {
    File file = Paths.get(this.getDataFolder().toPath().toString(), SERIALIZED_PATH_CACHE_FILENAME).toFile();
    try {
      //noinspection ResultOfMethodCallIgnored
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
        SERIALIZED_PATH_CACHE_FILENAME).toFile());
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
    SearchTracker<LocationCell, World> tracker = new SpigotSearchTracker();

    // Cache survival modes
    IndicatorSearch search = new IndicatorSearch(IndicatorSearch.SURVIVAL_MODES, s -> true);
    search.setTracker(tracker);
    search.searchCacheable();

    // Cache creative modes (flying)
    search = new IndicatorSearch(Collections.singleton(new FlyMode()), s -> true);
    search.setTracker(tracker);
    search.searchCacheable();
  }
}
