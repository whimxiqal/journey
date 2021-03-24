package edu.whimc.indicator;

import com.google.common.collect.Lists;
import edu.whimc.indicator.common.cache.TrailCache;
import edu.whimc.indicator.common.path.Link;
import edu.whimc.indicator.common.path.Mode;
import edu.whimc.indicator.common.search.TrailSearch;
import edu.whimc.indicator.spigot.cache.DebugManager;
import edu.whimc.indicator.spigot.cache.JourneyManager;
import edu.whimc.indicator.spigot.cache.NetherManager;
import edu.whimc.indicator.spigot.command.EndpointCommand;
import edu.whimc.indicator.spigot.command.IndicatorCommand;
import edu.whimc.indicator.spigot.command.TrailCommand;
import edu.whimc.indicator.spigot.cache.EndpointManager;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.path.mode.FlyMode;
import edu.whimc.indicator.spigot.path.mode.JumpMode;
import edu.whimc.indicator.spigot.path.mode.SwimMode;
import edu.whimc.indicator.spigot.path.mode.WalkMode;
import edu.whimc.indicator.spigot.search.IndicatorSearch;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Indicator extends JavaPlugin {

  private static Indicator instance;

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
    // Create caches
    this.endpointManager = new EndpointManager();
    this.netherManager = new NetherManager();
    this.debugManager = new DebugManager();
    this.journeyManager = new JourneyManager();
    this.trailCache = new TrailCache<>();

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
      Indicator.getInstance().getLogger().info("Starting initial trail searches...");

      TrailSearch<LocationCell, World> trailSearch = new TrailSearch<>();
      List<Mode<LocationCell, World>> noFly = Lists.newArrayList(new WalkMode(), new JumpMode(), new SwimMode());
      IndicatorSearch globalSearch = new IndicatorSearch(noFly, p -> true);
      Map<World, Set<Link<LocationCell, World>>> entryDomains = globalSearch.collectEntryDomains();
      Map<World, Set<Link<LocationCell, World>>> exitDomains = globalSearch.collectExitDomains();

      // Run all link <-> link searches with all common modes without flying
      globalSearch.findLinkTrails(trailSearch, entryDomains, exitDomains);

      globalSearch.registerMode(new FlyMode());

      // Run all link <-> link searches with all common modes with flying
      globalSearch.findLinkTrails(trailSearch, entryDomains, exitDomains);

      valid = true;
      Indicator.getInstance().getLogger().info("Finished initial trail searches");
    });
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
