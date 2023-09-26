package net.whimxiqal.journey.navigation;

import java.util.HashMap;
import java.util.Map;
import net.whimxiqal.journey.Builder;
import net.whimxiqal.journey.navigation.option.NavigatorOption;

/**
 * A builder for a {@link NavigatorFactory}.
 */
public class NavigatorFactoryBuilder implements Builder<NavigatorFactory> {

  private final String plugin;
  private final String navigatorType;
  private final Map<String, NavigatorOption<?>> options = new HashMap<>();
  private String permission;
  private NavigatorSupplier navigatorSupplier;

  NavigatorFactoryBuilder(String plugin, String navigatorType) {
    this.plugin = plugin;
    this.navigatorType = navigatorType;
  }

  /**
   * Set the permission required to create a navigator.
   *
   * @param permission the permission
   * @return the builder, for chaining
   */
  public NavigatorFactoryBuilder permission(String permission) {
    this.permission = permission;
    return this;
  }

  /**
   * Add an allowed option for this factory's navigators.
   *
   * @param option the option
   * @param <T>    the type of value for this option
   * @return the builder, for chaining
   */
  public <T> NavigatorFactoryBuilder option(NavigatorOption<T> option) {
    options.put(option.optionId(), option);
    return this;
  }

  /**
   * Set the supplier function for new navigators.
   *
   * @param supplier the supplier
   * @return the builder, for chaining
   */
  public NavigatorFactoryBuilder supplier(NavigatorSupplier supplier) {
    this.navigatorSupplier = supplier;
    return this;
  }

  @Override
  public NavigatorFactory build() {
    if (navigatorSupplier == null) {
      throw new IllegalStateException("Navigator factories must have a navigator supplier");
    }
    return new NavigatorFactoryImpl(plugin, navigatorType, permission, options, navigatorSupplier);
  }
}
