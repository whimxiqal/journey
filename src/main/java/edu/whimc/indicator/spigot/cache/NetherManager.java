package edu.whimc.indicator.spigot.cache;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.api.path.Link;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.path.NetherLink;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.NetherUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;

import java.util.*;
import java.util.stream.Collectors;

public class NetherManager implements Listener {

  private final Map<LocationCell, LocationCell> links = new HashMap<>();

  public void registerListeners(Indicator plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  public Collection<Link<LocationCell, World>> makeLinks() {
    return links.entrySet().stream()
        .map(entry -> new NetherLink(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPortalCreate(PortalCreateEvent e) {
    // TODO implement a way to create link here
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onEntityPortal(EntityPortalEvent e) {
    onPortal(e.getFrom(), e.getTo(), e.getEntity());
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerPortal(PlayerPortalEvent e) {
    onPortal(e.getFrom(), e.getTo(), e.getPlayer());
  }

  private void onPortal(Location from, Location to, Entity entity) {
    if (to == null) return;

    LocationCell origin = new LocationCell(from);

    // Wait 1 second, and check for a portal around where the player is now
    Bukkit.getScheduler().runTaskLater(Indicator.getInstance(), () -> {
      Location loc = entity.getLocation();

      // Origin portal
      Optional<NetherUtil.PortalGroup> originGroup = NetherUtil
          .locateAll(origin,
              8,
              origin.getY() - 8,
              origin.getY() + 8)
          .stream()
          .min(Comparator.comparingDouble(group ->
              group.port().distanceToSquared(new LocationCell(from))));
      if (!originGroup.isPresent()) return;  // We can't find the origin portal

      // Destination Portal
      Optional<NetherUtil.PortalGroup> destinationGroup = NetherUtil
          .locateAll(new LocationCell(loc),
          16,
          loc.getBlockY() - 16,
          loc.getBlockY() + 16)
          .stream()
          .min(Comparator.comparingDouble(group ->
              group.port().distanceToSquared(new LocationCell(to))));
      if (!destinationGroup.isPresent()) return;  // We can't find the destination portal

      List<LocationCell> linkedOrigins = new LinkedList<>();
      for (LocationCell originCell : originGroup.get().getBlocks()) {
        for (LocationCell destinationCell : destinationGroup.get().getBlocks()) {
          if (!links.containsKey(originCell)) {
            continue;
          }
          linkedOrigins.add(originCell);
          if (links.get(originCell).equals(destinationCell)) {
            return;  // We already have this portal link set up
          }
        }
      }

      // We don't have this portal link set up yet

      // Remove any connections with this origin (the portal link changed)
      linkedOrigins.forEach(links::remove);
      linkedOrigins.add(originGroup.get().port());

      // Add the portal
      LocationCell previous = links.put(originGroup.get().port(), destinationGroup.get().port());
      if (previous == null) {
        Indicator.getInstance()
            .getDebugManager()
            .broadcastDebugMessage(Format.debug("Added nether link:"));
        Indicator.getInstance()
            .getDebugManager()
            .broadcastDebugMessage(Format.debug(originGroup.get().port() + " -> "));
        Indicator.getInstance()
            .getDebugManager()
            .broadcastDebugMessage(Format.debug(destinationGroup.get().port().toString()));
      }
    }, 20);
  }

}
