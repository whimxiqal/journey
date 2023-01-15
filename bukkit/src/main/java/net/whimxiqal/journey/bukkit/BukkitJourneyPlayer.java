package net.whimxiqal.journey.bukkit;

import java.util.UUID;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import net.whimxiqal.journey.common.JourneyPlayer;
import net.whimxiqal.journey.common.navigation.Cell;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitJourneyPlayer extends JourneyPlayer {
  public BukkitJourneyPlayer(Player player) {
    super(player.getUniqueId(), player.getDisplayName());
  }

  private Player player() {
    return Bukkit.getPlayer(uuid);
  }

  @Override
  public Cell location() {
    return BukkitUtil.cell(player().getLocation());
  }
}
