package edu.whimc.indicator.common.config;

import org.jetbrains.annotations.NotNull;

public class IntegerSetting extends Setting<Integer> {
  IntegerSetting(@NotNull String path, @NotNull Integer defaultValue) {
    super(path, defaultValue, Integer.class);
  }

  @Override
  Integer parseValue(@NotNull String string) {
    return Integer.parseInt(string);
  }

  @Override
  @NotNull String printValue() {
    return getValue().toString();
  }
}
