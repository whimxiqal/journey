package net.whimxiqal.journey.search.flag;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class DoubleFlag extends Flag<Double> {
  private final double min;
  private final double max;
  private final List<Double> suggestions;

  protected DoubleFlag(String name, Supplier<Double> defaultValue, String permission, double min, double max, Double... suggestions) {
    super(name, defaultValue, permission, Double.class);
    this.min = min;
    this.max = max;
    this.suggestions = Arrays.asList(suggestions);
  }

  @Override
  public List<Double> suggestedValues() {
    return suggestions;
  }

  @Override
  public boolean valid(Double value) {
    return value >= min && value <= max;
  }
}
