package net.whimxiqal.journey.navigation;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.search.SearchStep;

public interface NavigationApi {

  void registerNavigator(NavigatorFactory navigatorFactory);

  Future<NavigationResult> navigate(NavigationRequest navigationRequest, JourneyAgent agent, List<SearchStep> path);

  TrailNavigationRequestBuilder trailNavigationRequestBuilder();

  int stopNavigation(UUID agentUuid);

}
