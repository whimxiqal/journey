package net.whimxiqal.journey.search.flag;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.navigation.NavigatorDetails;

public class NavigatorDetailsFlag extends Flag<NavigatorDetails> {
  public NavigatorDetailsFlag(String name, Supplier<NavigatorDetails> defaultValue, String permission) {
    super(name, defaultValue, permission, NavigatorDetails.class);
  }

  @Override
  public String printValue(NavigatorDetails val) {
    return val.navigatorType();
  }

  @Override
  public List<NavigatorDetails> suggestedValues() {
    return Journey.get().navigatorManager().navigators()
        .stream()
        .map(NavigatorDetails::of)
        .collect(Collectors.toList());
  }
}
