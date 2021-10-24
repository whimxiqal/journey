package edu.whimc.journey.common.config;

import org.jetbrains.annotations.NotNull;

public class IntegerSetting extends Setting<Integer> {
  IntegerSetting(@NotNull String path, @NotNull Integer defaultValue) {
    super(path, defaultValue, Integer.class);
  }

  @Override
  public Integer parseValue(@NotNull String string) {
    return Integer.parseInt(string);
  }

  @Override
  @NotNull
  public String printValue() {
    return getValue().toString();
  }
}
