package me.pietelite.journey.common.integration;

import java.util.Collections;
import java.util.HashMap;
import me.pietelite.journey.common.navigation.Cell;

public class RootScopeBuilder {

  private final HashMap<String, Scope> subScopes = new HashMap<>();

  public RootScopeBuilder subScope(Scope scope) {
    if (subScopes.containsKey(scope.name())) {
      throw new IllegalArgumentException("A sub-scope with name " + scope.name() + " already exists in root scope");
    }
    subScopes.put(scope.name(), scope);
    return this;
  }

  public Scope build() {
    return new Scope(null, subScopes, Collections.emptyMap());
  }

}
