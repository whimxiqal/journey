package me.pietelite.journey.spigot;

import java.util.UUID;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.navigation.PlatformProxy;
import me.pietelite.journey.spigot.music.Song;
import me.pietelite.journey.spigot.util.SpigotUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class SpigotPlatformProxy implements PlatformProxy {
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
}
