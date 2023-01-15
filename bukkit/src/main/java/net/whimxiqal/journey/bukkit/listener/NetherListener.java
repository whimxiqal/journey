package net.whimxiqal.journey.bukkit.listener;

import net.whimxiqal.journey.common.Journey;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class NetherListener implements Listener {

  /**
   * An event handler for when an entity goes through a portal.
   * In this case, we know for sure how a portal is linked, and it can be saved.
   *
   * @param e the event
   */
  @EventHandler(priority = EventPriority.LOW)
  public void onEntityPortal(EntityPortalEvent e) {
    Journey.get().netherManager().lookForPortal(BukkitUtil.cell(e.getFrom()), () -> BukkitUtil.cell(e.getEntity().getLocation()));
  }

  /**
   * An event handler for when a player goes through a portal.
   * In this case, we know for sure how a portal is linked, and it can be saved.
   *
   * @param e the event
   */
  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerPortal(PlayerPortalEvent e) {
    Journey.get().netherManager().lookForPortal(BukkitUtil.cell(e.getFrom()), () -> BukkitUtil.cell(e.getPlayer().getLocation()));
  }

}
