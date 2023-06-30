package net.whimxiqal.journey.search.flag;

import java.util.List;
import java.util.function.Supplier;

public class BooleanFlag extends Flag<Boolean> {
  protected BooleanFlag(String name,  Supplier<Boolean> defaultValue, String permission) {
    super(name, defaultValue, permission, Boolean.class);
  }

  @Override
  public List<Boolean> suggestedValues() {
    return List.of(false, true);
  }
}
