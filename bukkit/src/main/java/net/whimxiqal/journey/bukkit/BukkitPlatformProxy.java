/*
 * MIT License
 *
 * Copyright (c) whimxiqal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.whimxiqal.journey.bukkit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.InternalJourneyPlayer;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.bukkit.chunk.BukkitSessionJourneyBlock;
import net.whimxiqal.journey.bukkit.chunk.BukkitSessionJourneyChunk;
import net.whimxiqal.journey.bukkit.gui.JourneyGui;
import net.whimxiqal.journey.bukkit.music.Song;
import net.whimxiqal.journey.bukkit.navigation.mode.FlyRayTraceMode;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import net.whimxiqal.journey.chunk.ChunkId;
import net.whimxiqal.journey.math.Vector;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.navigation.PlatformProxy;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyChunk;
import net.whimxiqal.journey.search.AnimationManager;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.flag.FlagSet;
import net.whimxiqal.journey.search.flag.Flags;
import net.whimxiqal.journey.util.BStatsUtil;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class BukkitPlatformProxy implements PlatformProxy {

  /**
   * The height of the space filled with air to be considered the surface of the world.
   */
  private static final int AT_SURFACE_HEIGHT = 64;
  private final Metrics metrics;

  public BukkitPlatformProxy() {
    metrics = new Metrics(JourneyBukkit.get(), BStatsUtil.BSTATS_ID);
  }

  @Override
  public JourneyChunk toChunk(ChunkId chunkId) {
    World world = BukkitUtil.getWorld(chunkId.domain());
    return new BukkitSessionJourneyChunk(world.getChunkAt(chunkId.x(), chunkId.z()).getChunkSnapshot(), world.getUID());
  }

  @Override
  public JourneyBlock toBlock(Cell cell) {
    return new BukkitSessionJourneyBlock(cell, BukkitUtil.getBlock(cell), new FlagSet());
  }

  @Override
  public void playSuccess(UUID playerUuid) {
    Song.SUCCESS_CHORD.play(Bukkit.getPlayer(playerUuid));
  }

  @Override
  public void spawnDestinationParticle(UUID playerUuid, int domain, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null || !player.getWorld().equals(BukkitUtil.getWorld(domain))) {
      return;
    }
    player.spawnParticle(Particle.SPELL_WITCH, x, y, z, count, offsetX, offsetY, offsetZ, 0);
  }

  @Override
  public void spawnModeParticle(UUID playerUuid, ModeType type, int domain, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
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
    World world = BukkitUtil.getWorld(domain);
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
  public Collection<InternalJourneyPlayer> onlinePlayers() {
    return Bukkit.getOnlinePlayers().stream().map(BukkitJourneyPlayer::new).collect(Collectors.toList());
  }

  @Override
  public Optional<InternalJourneyPlayer> onlinePlayer(UUID uuid) {
    return Optional.ofNullable(Bukkit.getPlayer(uuid)).map(BukkitJourneyPlayer::new);
  }

  @Override
  public Optional<InternalJourneyPlayer> onlinePlayer(String name) {
    return Optional.ofNullable(Bukkit.getPlayer(name)).map(BukkitJourneyPlayer::new);
  }

  @Override
  public Optional<Cell> entityCellLocation(UUID entityUuid) {
    return Optional.ofNullable(Bukkit.getEntity(entityUuid)).map(entity -> BukkitUtil.cell(entity.getLocation()));
  }

  @Override
  public Optional<Vector> entityVector(UUID entityUuid) {
    return Optional.ofNullable(Bukkit.getEntity(entityUuid)).map(entity -> BukkitUtil.toLocalVector(entity.getLocation().toVector()));
  }

  @Override
  public void prepareDestinationSearchSession(SearchSession search, UUID playerUuid, FlagSet flags, Cell destination) {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return;
    }
    if (player.getAllowFlight() && flags.getValueFor(Flags.FLY)) {
      search.registerMode(new FlyRayTraceMode(search, destination));
    }
  }

  @Override
  public boolean isAtSurface(Cell cell) {
    int x = cell.blockX();
    int z = cell.blockZ();
    for (int y = cell.blockY() + 1; y <= Math.min(256, cell.blockY() + AT_SURFACE_HEIGHT); y++) {
      if (BukkitUtil.getBlock(new Cell(x, y, z, cell.domain())).getMaterial() != Material.AIR) {
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
    if (BukkitUtil.cell(player.getLocation()).equals(location)
        || BukkitUtil.cell(player.getLocation().add(0, 1, 0)).equals(location)) {
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
    if (BukkitUtil.getWorld(cell) != player.getWorld()
        || cell.distanceToSquared(BukkitUtil.cell(player.getLocation())) > 10000) {
      return false;
    }
    player.sendBlockChange(BukkitUtil.toLocation(cell), blockData);
    return true;
  }

  @Override
  public boolean resetBlockData(UUID playerUuid, Collection<Cell> locations) {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return false;
    }

    locations.forEach(cell -> showBlock(player, cell, BukkitUtil.getBlock(cell)));
    return true;
  }

  @Override
  public String domainName(int domain) {
    return BukkitUtil.getWorld(domain).getName();
  }

  @Override
  public boolean sendGui(JourneyPlayer player) {
    JourneyGui journeyGui = new JourneyGui(player);
    journeyGui.open();
    return true;
  }

  @Override
  public boolean synchronous() {
    return Bukkit.isPrimaryThread();
  }

  @Override
  public Consumer<CustomChart> bStatsChartConsumer() {
    return metrics::addCustomChart;
  }

  @Override
  public Map<String, Map<String, Integer>> domainResourceKeys() {
    Map<String, Map<String, Integer>> domains = new HashMap<>();
    for (World world : Bukkit.getWorlds()) {
      NamespacedKey key = world.getKey();
      domains.computeIfAbsent(key.namespace(), k -> new HashMap<>()).put(key.getKey(), BukkitUtil.getDomain(world));
    }
    return domains;
  }
}
