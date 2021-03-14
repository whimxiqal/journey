package edu.whimc.indicator;

import com.google.common.collect.Lists;
import edu.whimc.indicator.spigot.cache.DebugManager;
import edu.whimc.indicator.spigot.cache.NetherManager;
import edu.whimc.indicator.spigot.command.EndpointCommand;
import edu.whimc.indicator.spigot.command.IndicatorCommand;
import edu.whimc.indicator.spigot.command.TrailCommand;
import edu.whimc.indicator.spigot.cache.EndpointManager;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

public final class Indicator extends JavaPlugin {

  private static Indicator instance;

  // Caches
  @Getter
  private EndpointManager endpointManager;
  @Getter
  private NetherManager netherManager;
  @Getter
  private DebugManager debugManager;

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
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
