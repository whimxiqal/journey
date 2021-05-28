package edu.whimc.indicator.spigot.command.menu;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CustomLocationsMenu extends Menu {
  private CustomLocationsMenu() {
    super("Custom Locations", "Your saved custom locations", new ArrayList<>());
  }

  public static CustomLocationsMenu buildFor(Player player) {
    return new CustomLocationsMenu();
  }
}
