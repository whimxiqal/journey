package edu.whimc.indicator.spigot.util;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Function;

public class UuidToWorld implements Function<String, World>, Serializable {
  @Override
  public World apply(String s) {
    return Bukkit.getWorld(UUID.fromString(s));
  }
}
