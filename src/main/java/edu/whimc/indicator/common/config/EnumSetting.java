package edu.whimc.indicator.common.config;

import com.google.common.base.Enums;
import org.jetbrains.annotations.NotNull;

public class EnumSetting<E extends Enum<E>> extends Setting<E> {
  EnumSetting(@NotNull String path, @NotNull E defaultValue, @NotNull Class<E> clazz) {
    super(path, defaultValue, clazz);
  }

  @Override
  E parseValue(@NotNull String string) {
    return Enums.getIfPresent(this.clazz, string.toUpperCase()).orNull();
  }

  @Override
  @NotNull String printValue() {
    return getValue().name().toLowerCase();
  }
}
