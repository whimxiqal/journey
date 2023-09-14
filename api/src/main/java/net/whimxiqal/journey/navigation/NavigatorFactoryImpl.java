package net.whimxiqal.journey.navigation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.navigation.option.NavigatorOption;
import net.whimxiqal.journey.navigation.option.NavigatorOptionValues;
import net.whimxiqal.journey.search.SearchStep;
import org.jetbrains.annotations.Nullable;

public class NavigatorFactoryImpl implements NavigatorFactory {

  private final String plugin;
  private final String navigatorId;
  private final String permission;
  private final Map<String, NavigatorOption<?>> options;
  private final NavigatorSupplier navigatorSupplier;

  NavigatorFactoryImpl(String plugin, String navigatorId, @Nullable String permission,
                       Map<String, NavigatorOption<?>> options, NavigatorSupplier navigatorSupplier) {
    this.plugin = plugin;
    this.navigatorId = navigatorId;
    this.permission = permission;
    this.options = options;
    this.navigatorSupplier = navigatorSupplier;
  }

  @Override
  public String plugin() {
    return plugin;
  }

  @Override
  public String navigatorId() {
    return navigatorId;
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
  public Navigator navigator(JourneyAgent agent, NavigationProgress progress, NavigatorOptionValues optionValues) {
    return navigatorSupplier.navigator(agent, progress, optionValues);
  }
}
