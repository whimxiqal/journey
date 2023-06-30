package net.whimxiqal.journey.navigator;

public interface TrailNavigator extends Navigator {

  void setParticle(String particle) throws IllegalArgumentException;

  void setWidth(double width) throws IllegalArgumentException;

  void setDensity(double density) throws IllegalArgumentException;

}
