package net.whimxiqal.journey.spigot;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.whimxiqal.journey.common.math.Vector;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.navigation.ModeType;
import net.whimxiqal.journey.common.navigation.PlatformProxy;
import net.whimxiqal.journey.common.search.AnimationManager;
import net.whimxiqal.journey.common.search.SearchSession;
import net.whimxiqal.journey.common.search.flag.FlagSet;
import net.whimxiqal.journey.common.search.flag.Flags;
import net.whimxiqal.journey.spigot.external.whimcportals.WhimcPortalPort;
import net.whimxiqal.journey.spigot.music.Song;
import net.whimxiqal.journey.spigot.navigation.mode.BoatMode;
import net.whimxiqal.journey.spigot.navigation.mode.ClimbMode;
import net.whimxiqal.journey.spigot.navigation.mode.DigMode;
import net.whimxiqal.journey.spigot.navigation.mode.DoorMode;
import net.whimxiqal.journey.spigot.navigation.mode.FlyMode;
import net.whimxiqal.journey.spigot.navigation.mode.FlyRayTraceMode;
import net.whimxiqal.journey.spigot.navigation.mode.JumpMode;
import net.whimxiqal.journey.spigot.navigation.mode.SwimMode;
import net.whimxiqal.journey.spigot.navigation.mode.WalkMode;
import net.whimxiqal.journey.spigot.util.MaterialGroups;
import net.whimxiqal.journey.spigot.util.SpigotUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SpigotPlatformProxy implements PlatformProxy {

  /**
   * The height of the space filled with air to be considered the surface of the world.
   */
  private static final int AT_SURFACE_HEIGHT = 64;

  @Override
  public boolean isNetherPortal(Cell cell) {
    return SpigotUtil.getBlock(cell).getMaterial() == Material.NETHER_PORTAL;
  }

  @Override
  public void playSuccess(UUID playerUuid) {
    Song.SUCCESS_CHORD.play(Bukkit.getPlayer(playerUuid));
  }

  @Override
  public void spawnDestinationParticle(UUID playerUuid, String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null || !player.getWorld().equals(SpigotUtil.getWorld(domainId))) {
      return;
    }
    player.spawnParticle(Particle.SPELL_WITCH, x, y, z, count, offsetX, offsetY, offsetZ, 0);
  }

  @Override
  public void spawnModeParticle(UUID playerUuid, ModeType type, String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
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

    Player player = Bukkit.getPlayer(playerUuid);
    World world = SpigotUtil.getWorld(domainId);
    if (player == null || !player.getWorld().equals(world)) {
      return;
    }
    player.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 0);

    // Check if we need to "hint" where the trail is because the water obscures the particle
    if (world.getBlockAt(Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z)).isLiquid()
        && !world.getBlockAt(Location.locToBlock(x), Location.locToBlock(y + 1), Location.locToBlock(z)).isLiquid()) {
      world.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ);
    }
  }

  @Override
  public Optional<UUID> onlinePlayer(String name) {
    return Optional.ofNullable(Bukkit.getPlayer(name)).map(Entity::getUniqueId);
  }

  @Override
  public Optional<Cell> entityCellLocation(UUID entityUuid) {
    return Optional.ofNullable(Bukkit.getEntity(entityUuid)).map(entity -> SpigotUtil.cell(entity.getLocation()));
  }

  @Override
  public Optional<Vector> entityVector(UUID entityUuid) {
    return Optional.ofNullable(Bukkit.getEntity(entityUuid)).map(entity -> SpigotUtil.toLocalVector(entity.getLocation().toVector()));
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
  public void prepareDestinationSearchSession(SearchSession search, UUID playerUuid, FlagSet flags, Cell destination) {
    Set<Material> passableBlocks = new HashSet<>();
    if (flags.hasFlag(Flags.NO_DOOR)) {
      passableBlocks.add(Material.IRON_DOOR);
    }

    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return;
    }
    if (player.getAllowFlight() && !flags.hasFlag(Flags.NO_FLY)) {
      search.registerMode(new FlyRayTraceMode(search, passableBlocks, destination));
    }
  }

  @Override
  public boolean isAtSurface(Cell cell) {
    int x = cell.getX();
    int z = cell.getZ();
    for (int y = cell.getY() + 1; y <= Math.min(256, cell.getY() + AT_SURFACE_HEIGHT); y++) {
      if (SpigotUtil.getBlock(new Cell(x, y, z, cell.domainId())).getMaterial() != Material.AIR) {
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

    locations.forEach(cell -> showBlock(player, cell, SpigotUtil.getBlock(cell)));
    return true;
  }
}
