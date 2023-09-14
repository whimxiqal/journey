package net.whimxiqal.journey.navigation;

import java.util.Map;
import net.whimxiqal.journey.Permissible;
import net.whimxiqal.journey.navigation.option.NavigatorOption;

public interface NavigatorFactory extends NavigatorSupplier, Permissible {

  String plugin();

  String navigatorId();

  Map<String, NavigatorOption<?>> options();

  static NavigatorFactoryBuilder builder(String plugin, String navigatorId) {
    return new NavigatorFactoryBuilder(plugin, navigatorId);
  }

}
