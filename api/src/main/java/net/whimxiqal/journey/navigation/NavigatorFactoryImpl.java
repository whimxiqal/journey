package net.whimxiqal.journey.navigation;

import java.util.Map;
import java.util.Optional;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.navigation.option.NavigatorOption;
import net.whimxiqal.journey.navigation.option.NavigatorOptionValues;
import org.jetbrains.annotations.Nullable;

class NavigatorFactoryImpl implements NavigatorFactory {

  private final String plugin;
  private final String navigatorType;
  private final String permission;
  private final Map<String, NavigatorOption<?>> options;
  private final NavigatorSupplier navigatorSupplier;

  NavigatorFactoryImpl(String plugin, String navigatorType, @Nullable String permission,
                       Map<String, NavigatorOption<?>> options, NavigatorSupplier navigatorSupplier) {
    this.plugin = plugin;
    this.navigatorType = navigatorType;
    this.permission = permission;
    this.options = options;
    this.navigatorSupplier = navigatorSupplier;
  }

  @Override
  public String plugin() {
    return plugin;
  }

  @Override
  public String navigatorType() {
    return navigatorType;
  }

  @Override
  public Optional<String> permission() {
    return Optional.ofNullable(permission);
  }

  @Override
  public Map<String, NavigatorOption<?>> options() {
    return options;
  }

  @Override
  public Navigator navigator(JourneyAgent agent,
                             NavigationProgress progress,
                             NavigatorOptionValues optionValues) {
    return navigatorSupplier.navigator(agent, progress, optionValues);
  }
}
