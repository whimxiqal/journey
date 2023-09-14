package net.whimxiqal.journey.navigation;

import java.util.Collections;
import java.util.Map;
import net.whimxiqal.journey.navigation.option.NavigatorOptionValues;
import org.jetbrains.annotations.NotNull;

public record NavigationRequest(String navigatorId, Map<String, Object> options) implements Comparable<NavigationRequest> {

  public static NavigationRequest of(String navigatorId) {
    return new NavigationRequest(navigatorId, Collections.emptyMap());
  }

  @Override
  public int compareTo(@NotNull NavigationRequest o) {
    return navigatorId.compareTo(o.navigatorId);
  }
}
