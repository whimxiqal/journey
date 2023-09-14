package net.whimxiqal.journey;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import net.whimxiqal.journey.navigation.NavigationApi;
import net.whimxiqal.journey.navigation.NavigationRequest;
import net.whimxiqal.journey.navigation.NavigationResult;
import net.whimxiqal.journey.navigation.NavigatorFactory;
import net.whimxiqal.journey.navigation.TrailNavigationRequestBuilder;
import net.whimxiqal.journey.navigation.TrailNavigationRequestBuilderImpl;
import net.whimxiqal.journey.search.SearchStep;

public class NavigationApiImpl implements NavigationApi {
  @Override
  public void registerNavigator(NavigatorFactory navigatorFactory) {
    Journey.get().navigatorManager().registerNavigatorFactory(navigatorFactory);
  }

  @Override
  public Future<NavigationResult> navigate(NavigationRequest navigationRequest, JourneyAgent agent, List<SearchStep> path) {
    return Journey.get().navigatorManager().startNavigating(navigationRequest, agent, path);
  }

  @Override
  public TrailNavigationRequestBuilder trailNavigationRequestBuilder() {
    return new TrailNavigationRequestBuilderImpl();
  }

  @Override
  public int stopNavigation(UUID agentUuid) {
    return Journey.get().navigatorManager().stopNavigators(agentUuid);
  }
}
