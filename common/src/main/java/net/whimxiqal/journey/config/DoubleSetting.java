package net.whimxiqal.journey.config;

import org.jetbrains.annotations.NotNull;

public class DoubleSetting extends Setting<Double> {
  private final double min;
  private final double max;

  DoubleSetting(@NotNull String path, double defaultValue, boolean reloadable, double min, double max) {
    super(path, defaultValue, Double.class, reloadable);
    this.min = min;
    this.max = max;
  }

  @Override
  public boolean valid(Double value) {
    return value >= min && value <= max;
  }
}
