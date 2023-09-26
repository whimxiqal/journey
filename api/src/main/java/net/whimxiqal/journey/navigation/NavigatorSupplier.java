package net.whimxiqal.journey.navigation;

import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.navigation.option.NavigatorOptionValues;

/**
 * A supplier of {@link Navigator}s.
 */
@FunctionalInterface
public interface NavigatorSupplier {

  /**
   * Creates a new navigator.
   *
   * @param agent        the agent navigating using the navigator
   * @param progress     the supplier of information about the agent's progress along their designated path
   * @param optionValues the supplier of navigator option values
   * @return the navigator
   */
  Navigator navigator(JourneyAgent agent, NavigationProgress progress, NavigatorOptionValues optionValues);

}
