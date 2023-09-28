package net.whimxiqal.journey.config;

import java.util.List;
import net.whimxiqal.journey.navigation.option.Color;
import net.whimxiqal.journey.util.ColorUtil;
import org.jetbrains.annotations.NotNull;

public class ColorListSetting extends ListSetting<Color> {
  ColorListSetting(@NotNull String path, @NotNull List<Color> defaultValue, boolean reloadable) {
    super(path, defaultValue, Color.class, reloadable);
  }

  @Override
  public boolean validSubtype(Color value) {
    return ColorUtil.valid(value);
  }

}
