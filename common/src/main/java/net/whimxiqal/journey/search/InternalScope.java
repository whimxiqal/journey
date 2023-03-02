package net.whimxiqal.journey.search;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.Scope;
import net.whimxiqal.journey.VirtualMap;
import net.whimxiqal.journey.util.Permission;

/**
 * Wrapper around public-facing {@link Scope}s which stores {@link SearchSession}s as the base component
 * (rather than {@link net.whimxiqal.journey.Destination}s.
 */
public class InternalScope {

  private final Scope scope;
  /**
   * Sessions in addition to those created by the {@link Scope}.
   */
  private final Function<JourneyPlayer, VirtualMap<SearchSession>> sessions;
  /**
   * Internal sub-scopes in addition to the sub-scopes contained in the {@link Scope}.
   */
  private final Function<JourneyPlayer, VirtualMap<InternalScope>> internalSubScopes;

  public InternalScope(Scope scope) {
    this(scope, player -> VirtualMap.empty(), player -> VirtualMap.empty());
  }

  public InternalScope(Scope scope, Function<JourneyPlayer, VirtualMap<SearchSession>> sessions, Function<JourneyPlayer, VirtualMap<InternalScope>> internalScopes) {
    this.scope = scope;
    this.sessions = player -> VirtualMap.of(() -> {
      Map<String, SearchSession> sessionMap = new HashMap<>(sessions.apply(player).getAll());
      scope.destinations(player).getAll().forEach((name, destination) -> {
        SearchSession session = new PlayerDestinationGoalSearchSession(player.uuid(), player.location(), destination.location(), true);
        session.setName(destination.name());
        session.setDescription(destination.description());
        destination.permission().ifPresent(perm -> {
          session.addPermission(perm);
          if (!perm.startsWith(Permission.JOURNEY_PATH_PERMISSION_PREFIX)) {
            session.addPermission(Permission.journeyPathExtend(perm));
          }
        });
        sessionMap.put(name, session);
      });
      return sessionMap;
    }, sessions.apply(player).size() + scope.destinations(player).size());
    this.internalSubScopes = player -> VirtualMap.of(() -> {
      Map<String, InternalScope> scopesMap = new HashMap<>(internalScopes.apply(player).getAll());
      scope.subScopes(player).getAll().forEach((name, subScope) -> {
        scopesMap.put(name, new InternalScope(subScope));
      });
      return scopesMap;
    }, internalScopes.apply(player).size() + scope.subScopes(player).size());
  }

  public VirtualMap<InternalScope> subScopes(JourneyPlayer player) {
    return internalSubScopes.apply(player);
  }

  public VirtualMap<SearchSession> sessions(JourneyPlayer player) {
    return sessions.apply(player);
  }

  public boolean isStrict() {
    return scope.isStrict();
  }

  public Scope wrappedScope() {
    return scope;
  }

}
