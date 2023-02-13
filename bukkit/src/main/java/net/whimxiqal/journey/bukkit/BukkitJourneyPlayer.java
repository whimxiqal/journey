package net.whimxiqal.journey.bukkit;

import net.kyori.adventure.audience.Audience;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyPlayerImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitJourneyPlayer extends JourneyPlayerImpl {

  public BukkitJourneyPlayer(Player player) {
    super(player.getUniqueId(), player.getName());
  }

  private Player player() {
    return Bukkit.getPlayer(uuid);
  }

  @Override
  public Cell location() {
    return BukkitUtil.cell(player().getLocation());
  }

  @Override
  public boolean hasPermission(String permission) {
    return player().hasPermission(permission);
  }

  @Override
  public Audience audience() {
    return Journey.get().proxy().audienceProvider().player(uuid);
  }

}
