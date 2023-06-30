package net.whimxiqal.journey.search.flag;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class IntegerFlag extends Flag<Integer> {
  private final int min;
  private final int max;
  private final List<Integer> suggestions;

  protected IntegerFlag(String name, Supplier<Integer> defaultValue, String permission, int min, int max, Integer... suggestions) {
    super(name, defaultValue, permission, Integer.class);
    this.min = min;
    this.max = max;
    this.suggestions = Arrays.asList(suggestions);
  }

  @Override
  public List<Integer> suggestedValues() {
    return suggestions;
  }

  @Override
  public boolean valid(Integer value) {
    return value >= min && value <= max;
  }
}
