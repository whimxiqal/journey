package me.pietelite.journey.common.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import me.pietelite.journey.common.navigation.Cell;

public class ScopeBuilder {

  private final String name;
  private final HashMap<String, Scope> subScopes = new HashMap<>();
  private final HashMap<String, Cell> items = new HashMap<>();

  public ScopeBuilder(String name) {
    this.name = name;
  }

  public ScopeBuilder subScope(Scope scope) {
    if (subScopes.containsKey(scope.name())) {
      throw new IllegalArgumentException("A sub-scope with name " + scope.name() + " already exists in scope " + name);
    }
    subScopes.put(scope.name(), scope);
    return this;
  }

  public ScopeBuilder item(String item, Cell location) {
    if (items.containsKey(item)) {
      throw new IllegalArgumentException("Root scope already contains item " + item);
    }
    items.put(item, location);
    return this;
  }

  public ScopeBuilder items(Map<String, Cell> items) {
    for (Map.Entry<String, Cell> item : items.entrySet()) {
      item(item.getKey(), item.getValue());
    }
    return this;
  }

  public ScopeBuilder items(Supplier<Map<String, Cell>> items) {
    items(items.get());
    return this;
  }

  public Scope build() {
    return new Scope(name, subScopes, items);
  }

}
