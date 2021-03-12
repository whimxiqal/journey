package edu.whimc.indicator.spigot.destination;

import edu.whimc.indicator.api.path.Endpoint;
import edu.whimc.indicator.spigot.path.CellImpl;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class EndpointImpl extends Endpoint<JavaPlugin, CellImpl, World> {
  public EndpointImpl(JavaPlugin purpose, CellImpl location) {
    super(purpose, location);
  }
}
