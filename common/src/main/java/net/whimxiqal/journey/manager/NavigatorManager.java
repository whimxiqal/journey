package net.whimxiqal.journey.manager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.whimxiqal.journey.navigator.Navigator;
import net.whimxiqal.journey.navigator.NavigatorSupplier;

public class NavigatorManager {

  private final Map<String, NavigatorSupplier> navigatorSuppliers = new HashMap<>();
  private final Map<UUID, Navigator> activeNavigators = new HashMap<>();

  public void registerNavigator(String id, NavigatorSupplier navigatorSupplier) {
    if (navigatorSuppliers.containsKey(id.toLowerCase(Locale.ENGLISH))) {
      throw new IllegalArgumentException("A navigator supplier with id " + id + " already exists!");
    }
    navigatorSuppliers.put(id.toLowerCase(Locale.ENGLISH), navigatorSupplier);
  }

  // TODO write other navigator stuff, bringing them over from SearchManager, and then also hook into API impl

}
