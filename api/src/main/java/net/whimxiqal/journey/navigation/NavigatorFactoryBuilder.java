package net.whimxiqal.journey.navigation;

import java.util.HashMap;
import java.util.Map;
import net.whimxiqal.journey.Builder;
import net.whimxiqal.journey.navigation.option.NavigatorOption;

public class NavigatorFactoryBuilder implements Builder<NavigatorFactory> {

  private final String plugin;
  private final String navigatorId;
  private String permission;
  private final Map<String, NavigatorOption<?>> options = new HashMap<>();
  private NavigatorSupplier navigatorSupplier;

  NavigatorFactoryBuilder(String plugin, String navigatorId) {
    this.plugin = plugin;
    this.navigatorId = navigatorId;
  }

  public NavigatorFactoryBuilder permission(String permission) {
    this.permission = permission;
    return this;
  }

  public <T> NavigatorFactoryBuilder option(NavigatorOption<T> option) {
    options.put(option.optionId(), option);
    return this;
  }

  public NavigatorFactoryBuilder supplier(NavigatorSupplier supplier) {
    this.navigatorSupplier = supplier;
    return this;
  }

  @Override
  public NavigatorFactory build() {
    if (navigatorSupplier == null) {
      throw new IllegalStateException("Navigator factories must have a navigator supplier");
    }
    return new NavigatorFactoryImpl(plugin, navigatorId, permission, options, navigatorSupplier);
  }
}
