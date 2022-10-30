package me.pietelite.journey.spigot.integration;

import me.pietelite.journey.common.integration.PotentialIntegrator;
import org.bukkit.Bukkit;

public class PotentialEssentialsIntegrator implements PotentialIntegrator<EssentialsIntegrator> {

  @Override
  public EssentialsIntegrator integrator() {
    return new EssentialsIntegrator();
  }

  @Override
  public boolean viable() {
    return Bukkit.getPluginManager().isPluginEnabled("essentials");
  }

}
