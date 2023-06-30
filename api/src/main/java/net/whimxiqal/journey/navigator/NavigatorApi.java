package net.whimxiqal.journey.navigator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.whimxiqal.journey.search.SearchStep;

public interface NavigatorApi {

  <T extends Navigator> void registerNavigator(String plugin, String id, NavigatorSupplier<T> navigatorSupplier, Class<T> clazz);

  <T extends Navigator> Optional<NavigatorSupplier<T>> provideNavigator(Class<T> clazz);

  <T extends Navigator> Optional<T> providePlayerNavigator(Class<T> clazz, UUID playerUuid, List<SearchStep> path);

  Optional<NavigatorSupplier<?>> provideNavigator(String navigatorId);

  Optional<Navigator> providePlayerNavigator(String navigatorId, UUID playerUuid, List<SearchStep> path);

  void navigate(Navigator navigator);

}
