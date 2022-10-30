package me.pietelite.journey.common.integration;

public interface PotentialIntegrator<I extends Integrator> {

  I integrator();

  boolean viable();

}
