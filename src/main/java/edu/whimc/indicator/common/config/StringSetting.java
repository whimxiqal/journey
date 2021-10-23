package edu.whimc.indicator.common.config;

import org.jetbrains.annotations.NotNull;

public class StringSetting extends Setting<String> {
  StringSetting(@NotNull String path, @NotNull String defaultValue) {
    super(path, defaultValue, String.class);
  }

  @Override
  public String parseValue(@NotNull String string) {
    return string;
  }

  @Override
  @NotNull
  public String printValue() {
    return getValue();
  }
}
