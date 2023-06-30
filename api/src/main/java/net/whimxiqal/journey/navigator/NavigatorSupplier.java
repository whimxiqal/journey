package net.whimxiqal.journey.navigator;

import java.util.List;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.navigator.Navigator;
import net.whimxiqal.journey.search.SearchStep;

@FunctionalInterface
public interface NavigatorSupplier<T extends Navigator> {

  T navigator(JourneyAgent agent, List<SearchStep> path);

}
