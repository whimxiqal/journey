package net.whimxiqal.journey.search.flag;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.navigation.NavigationRequest;

public class NavigationRequestFlag extends Flag<NavigationRequest> {
  public NavigationRequestFlag(String name, Supplier<NavigationRequest> defaultValue, String permission) {
    super(name, defaultValue, permission, NavigationRequest.class);
  }

  @Override
  public String printValue(NavigationRequest val) {
    return val.navigatorId();
  }

  @Override
  public List<NavigationRequest> suggestedValues() {
    return Journey.get().navigatorManager().navigators()
        .stream()
        .map(NavigationRequest::of)
        .collect(Collectors.toList());
  }
}
