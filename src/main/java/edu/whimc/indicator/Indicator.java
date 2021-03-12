package edu.whimc.indicator;

import com.google.common.collect.Lists;
import edu.whimc.indicator.spigot.command.EndpointCommand;
import edu.whimc.indicator.spigot.command.TrailCommand;
import edu.whimc.indicator.spigot.destination.EndpointManager;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

public final class Indicator extends JavaPlugin {

  private static Indicator instance;

  @Getter
  private EndpointManager endpointManager;

  public static Indicator getInstance() {
    return instance;
  }

  @Override
  public void onLoad() {
    instance = this;
  }

  @Override
  public void onEnable() {
    this.endpointManager = new EndpointManager();

    // Register commands
    Lists.newArrayList(new TrailCommand(), new EndpointCommand()).forEach(root -> {
      PluginCommand command = getCommand(root.getPrimaryAlias());
      if (command == null) {
        throw new NullPointerException("You must register this command in the plugin.yml");
      }
      command.setExecutor(root);
      command.setTabCompleter(root);
      root.getPermission().map(Permission::getName).ifPresent(command::setPermission);
    });
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
