package net.whimxiqal.journey.navigation;

import java.util.Map;
import net.whimxiqal.journey.Permissible;
import net.whimxiqal.journey.navigation.option.NavigatorOption;

/**
 * A factory for and source of information about a specific type of {@link Navigator}.
 */
public interface NavigatorFactory extends NavigatorSupplier, Permissible {

  /**
   * Static constructor for a builder of a {@link NavigatorFactory}.
   *
   * @param plugin        the creating plugin
   * @param navigatorType the type of navigator
   * @return the builder
   */
  static NavigatorFactoryBuilder builder(String plugin, String navigatorType) {
    return new NavigatorFactoryBuilder(plugin, navigatorType);
  }

  /**
   * The plugin responsible for this factory and its resultant navigators.
   *
   * @return the plugin
   */
  String plugin();

  /**
   * The type (id) of the navigator.
   *
   * @return the navigator type
   */
  String navigatorType();

  /**
   * The options for this factory's resultant navigators.
   * A user of one of this factory's navigators may use these options
   * and new option values to manipulate the behavior of the navigator.
   *
   * @return the options allowed on this factory's navigators
   */
  Map<String, NavigatorOption<?>> options();

}
