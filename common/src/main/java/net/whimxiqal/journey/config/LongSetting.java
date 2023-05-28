package net.whimxiqal.journey.config;

import org.jetbrains.annotations.NotNull;

public class LongSetting extends Setting<Long> {
  private final long min;
  private final long max;

  LongSetting(@NotNull String path, long defaultValue, long min, long max) {
    super(path, defaultValue, Long.class);
    this.min = min;
    this.max = max;
  }

  @Override
  public Long parseValue(@NotNull String string) {
    return Long.parseLong(string);
  }

  @Override
  @NotNull
  public String printValue() {
    return getValue().toString();
  }

  @Override
  public boolean isValid() {
    return value >= min && value <= max;
  }
}
