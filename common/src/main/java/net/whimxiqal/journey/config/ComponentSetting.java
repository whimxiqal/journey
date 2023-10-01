package net.whimxiqal.journey.config;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ComponentSetting extends Setting<Component> {
  ComponentSetting(@NotNull String path, @NotNull Component defaultValue, boolean reloadable) {
    super(path, defaultValue, Component.class, reloadable);
  }
}
