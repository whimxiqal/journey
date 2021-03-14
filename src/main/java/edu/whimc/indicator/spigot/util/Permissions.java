package edu.whimc.indicator.spigot.util;

import org.bukkit.permissions.Permission;

public final class Permissions {

  private Permissions() {
  }

  public static Permission INDICATOR_PERMISSION = new Permission("indicator.indicator");
  public static Permission DEBUG_PERMISSION = new Permission("indicator.debug");
  public static Permission TRAIL_PERMISSION = new Permission("indicator.trail");
  public static Permission DESTINATION_PERMISSION = new Permission("indicator.destination");

}
