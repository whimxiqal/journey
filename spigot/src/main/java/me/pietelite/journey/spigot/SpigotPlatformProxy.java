package me.pietelite.journey.spigot;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.navigation.PlatformProxy;
import me.pietelite.journey.common.search.AnimationManager;
import me.pietelite.journey.common.search.SearchSession;
import me.pietelite.journey.common.search.flag.FlagSet;
import me.pietelite.journey.common.search.flag.Flags;
import me.pietelite.journey.spigot.external.whimcportals.WhimcPortalPort;
import me.pietelite.journey.spigot.music.Song;
import me.pietelite.journey.spigot.navigation.mode.BoatMode;
import me.pietelite.journey.spigot.navigation.mode.ClimbMode;
import me.pietelite.journey.spigot.navigation.mode.DigMode;
import me.pietelite.journey.spigot.navigation.mode.DoorMode;
import me.pietelite.journey.spigot.navigation.mode.FlyMode;
import me.pietelite.journey.spigot.navigation.mode.JumpMode;
import me.pietelite.journey.spigot.navigation.mode.SwimMode;
import me.pietelite.journey.spigot.navigation.mode.WalkMode;
import me.pietelite.journey.spigot.util.MaterialGroups;
import me.pietelite.journey.spigot.util.SpigotUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class SpigotPlatformProxy implements PlatformProxy {

  /**
   * The height of the space filled with air to be considered the surface of the world.
   */
  private static final int AT_SURFACE_HEIGHT = 64;

  @Override
  public boolean isNetherPortal(Cell cell) {
    return false;
  }

  @Override
  public void playSuccess(UUID playerUuid) {
    Song.SUCCESS_CHORD.play(Bukkit.getPlayer(playerUuid));
  }

  @Override
  public void spawnDestinationParticle(String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {

  }

  @Override
  public void spawnModeParticle(ModeType type, String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
    Particle particle;
    int particleCount;
    int multiplier;
    if (type == ModeType.FLY) {
      particle = Particle.WAX_OFF;
    } else if (type == ModeType.DIG) {
      particle = Particle.CRIT;
      count *= 5;
    } else {
      particle = Particle.GLOW;
    }

    World world = SpigotUtil.getWorld(domainId);
    world.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 0);

    // Check if we need to "hint" where the trail is because the water obscures the particle
    if (world.getBlockAt(Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z)).isLiquid()
        && !world.getBlockAt(Location.locToBlock(x), Location.locToBlock(y + 1), Location.locToBlock(z)).isLiquid()) {
      world.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ);
    }
  }

  @Override
  public Optional<UUID> onlinePlayer(String name) {
    return Optional.empty();
  }

  @Override
  public Cell entityLocation(UUID entityUuid) {
    return null;
  }

  @Override
  public void prepareSearchSession(SearchSession search, UUID playerUuid, FlagSet flags, boolean includePorts) {
    // MODES
    Set<Material> passableBlocks = new HashSet<>();
    if (flags.hasFlag(Flags.NO_DOOR)) {
      passableBlocks.add(Material.IRON_DOOR);
    }

    // Register modes in order of preference
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return;
    }
    if (player.getAllowFlight() && !flags.hasFlag(Flags.NO_FLY)) {
      search.registerMode(new FlyMode(search, passableBlocks));
    } else {
      search.registerMode(new WalkMode(search, passableBlocks));
      search.registerMode(new JumpMode(search, passableBlocks));
      search.registerMode(new SwimMode(search, passableBlocks));
      if (MaterialGroups.BOATS.stream().anyMatch(boatType -> player.getInventory().contains(boatType))) {
        search.registerMode(new BoatMode(search, passableBlocks));
      }
    }
    search.registerMode(new DoorMode(search, passableBlocks));
    search.registerMode(new ClimbMode(search, passableBlocks));
    if (flags.hasFlag(Flags.DIG)) {
      search.registerMode(new DigMode(search, passableBlocks));
    }

    // PORTS
    if (includePorts) {
      WhimcPortalPort.addPortsTo(search, player::hasPermission);
    }
  }

  @Override
  public boolean isAtSurface(Cell cell) {
    int x = cell.getX();
    int z = cell.getZ();
    World world = SpigotUtil.getWorld(cell);
    for (int y = cell.getY() + 1; y <= Math.min(256, cell.getY() + AT_SURFACE_HEIGHT); y++) {
      if (world.getBlockAt(x, y, z).getType() != Material.AIR) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean sendBlockData(UUID playerUuid, Cell location, AnimationManager.StageType stage, ModeType mode) {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return false;
    }
    if (SpigotUtil.cell(player.getLocation()).equals(location)
        || SpigotUtil.cell(player.getLocation().add(0, 1, 0)).equals(location)) {
      return false;
    }

    switch (stage) {
      case SUCCESS: {
        BlockData blockData;
        switch (mode) {
          case WALK:
            blockData = Material.LIME_STAINED_GLASS.createBlockData();
            break;
          case JUMP:
            blockData = Material.MAGENTA_STAINED_GLASS.createBlockData();
            break;
          case FLY:
            blockData = Material.WHITE_STAINED_GLASS.createBlockData();
            break;
          default:
            blockData = Material.COBWEB.createBlockData();
            break;
        }
        return showBlock(player, location, blockData);
      }
      case FAILURE:
        return showBlock(player, location, Material.GLOWSTONE.createBlockData());
      case STEP:
        return showBlock(player, location, Material.OBSIDIAN.createBlockData());
    }
    return true;
  }

  private boolean showBlock(Player player, Cell cell, BlockData blockData) {
    if (SpigotUtil.getWorld(cell) != player.getWorld()
        || cell.distanceToSquared(SpigotUtil.cell(player.getLocation())) > 10000) {
      return false;
    }
    player.sendBlockChange(SpigotUtil.toLocation(cell), blockData);
    return true;
  }

  @Override
  public boolean resetBlockData(UUID playerUuid, Collection<Cell> locations) {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return false;
    }

    player.sendBlockChanges(locations.stream().map(loc -> SpigotUtil.getBlock(loc).getState()).collect(Collectors.toList()), true);
    locations.forEach(cell -> showBlock(player, cell, SpigotUtil.getBlock(cell).getBlockData()));
    return true;
  }
}
