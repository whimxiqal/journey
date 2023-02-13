package net.whimxiqal.journey.bukkit;

import net.whimxiqal.journey.bukkit.JourneyBukkitApi;
import net.whimxiqal.journey.bukkit.JourneyBukkitApiProvider;

public final class JourneyBukkitApiSupplier {

  private JourneyBukkitApiSupplier() {
  }

  public static void set(JourneyBukkitApi api) {
    JourneyBukkitApiProvider.provide(api);
  }

}
