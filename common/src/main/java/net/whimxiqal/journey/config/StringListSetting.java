package net.whimxiqal.journey.config;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class StringListSetting extends ListSetting<String> {

  protected StringListSetting(@NotNull String path, @NotNull List<String> defaultValue, boolean reloadable) {
    super(path, defaultValue, String.class, reloadable);
  }

}
