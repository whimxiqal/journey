package net.whimxiqal.journey.search.flag;

import java.util.List;
import java.util.function.Supplier;

public class StringFlag extends Flag<String> {
  private final Supplier<List<String>> suggestedValues;
  public StringFlag(String name, Supplier<String> defaultValue, String permission, Supplier<List<String>> suggestedValues) {
    super(name, defaultValue, permission, String.class);
    this.suggestedValues = suggestedValues;
  }

  @Override
  public List<String> suggestedValues() {
    return suggestedValues.get();
  }
}
