package edu.whimc.indicator;

import com.google.common.collect.Lists;
import edu.whimc.indicator.command.DestinationCommand;
import edu.whimc.indicator.command.TrailCommand;
import edu.whimc.indicator.destination.DestinationManager;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

public final class Indicator extends JavaPlugin {

  private static Indicator instance;

  @Getter
  private DestinationManager destinationManager;

  public static Indicator getInstance() {
    return instance;
  }

  @Override
  public void onLoad() {
    instance = this;
  }

  @Override
  public void onEnable() {
    this.destinationManager = new DestinationManager();

    // Register commands
    Lists.newArrayList(new TrailCommand(), new DestinationCommand()).forEach(root -> {
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
