package net.whimxiqal.journey.bukkit.listener;

import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.bukkit.JourneyBukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class PluginDisableListener implements Listener {

  @EventHandler
  public void onPluginDisable(PluginDisableEvent event) {
    if (!JourneyBukkit.get().isEnabled()) {
      // Journey is already disabled
      return;
    }
    String name = event.getPlugin().getName();
    Journey.get().navigatorManager().stopNavigatorsDependentOn(name);
  }
}
