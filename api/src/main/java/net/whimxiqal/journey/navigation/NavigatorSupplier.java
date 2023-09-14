package net.whimxiqal.journey.navigation;

import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.navigation.option.NavigatorOptionValues;

@FunctionalInterface
public interface NavigatorSupplier {

  Navigator navigator(JourneyAgent agent, NavigationProgress progress, NavigatorOptionValues optionValues);

}
