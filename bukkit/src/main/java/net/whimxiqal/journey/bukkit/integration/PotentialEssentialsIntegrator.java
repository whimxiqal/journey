package net.whimxiqal.journey.bukkit.integration;

import net.whimxiqal.journey.common.data.integration.Integrator;
import net.whimxiqal.journey.common.data.integration.PotentialIntegrator;
import org.bukkit.Bukkit;

public class PotentialEssentialsIntegrator implements PotentialIntegrator {

  @Override
  public Integrator integrator() {
    return new EssentialsIntegrator();
  }

  @Override
  public boolean viable() {
    return Bukkit.getPluginManager().isPluginEnabled("essentials");
  }

}
