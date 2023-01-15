/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.whimxiqal.journey.common.data.integration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.common.navigation.Cell;

public class ScopeBuilder {

  private final String id;
  private final HashMap<String, Scope> subScopes = new HashMap<>();
  private final List<Consumer<Map<String, Cell>>> itemMutators = new LinkedList<>();
  private Component name;
  private Component description;

  public ScopeBuilder(String id) {
    this.id = id;
  }

  public ScopeBuilder name(Component name) {
    this.name = name;
    return this;
  }

  public ScopeBuilder description(Component description) {
    this.description = description;
    return this;
  }

  public ScopeBuilder addSubScope(Scope scope) {
    if (subScopes.containsKey(scope.id())) {
      throw new IllegalArgumentException("A sub-scope with id " + scope.id() + " already exists in scope " + name);
    }
    subScopes.put(scope.id(), scope);
    return this;
  }

  public ScopeBuilder addSubScopes(Collection<Scope> scopes) {
    scopes.forEach(this::addSubScope);
    return this;
  }

  private void accumulate(Map<String, Cell> accumulator, String item, Cell location) {
    if (accumulator.containsKey(item)) {
      throw new IllegalArgumentException("Programmer error: root scope already contains an item named " + item
          + ". All items in a single scope should have unique names.");
    }
    accumulator.put(item, location);
  }

  public ScopeBuilder addItem(String item, Cell location) {
    itemMutators.add(accumulator -> {
      accumulate(accumulator, item, location);
    });
    return this;
  }

  public ScopeBuilder addItems(Map<String, Cell> items) {
    itemMutators.add(accumulator -> {
      for (Map.Entry<String, Cell> item : items.entrySet()) {
        accumulate(accumulator, item.getKey(), item.getValue());
      }
    });
    return this;
  }

  public ScopeBuilder addItems(Supplier<Map<String, Cell>> items) {
    itemMutators.add(accumulator -> {
      for (Map.Entry<String, Cell> item : items.get().entrySet()) {
        accumulate(accumulator, item.getKey(), item.getValue());
      }
    });
    return this;
  }

  private Component calculateName() {
    if (name != null) {
      return name;
    }
    if (id != null) {
      return Component.text(id);
    }
    return Component.text("Home Page");
  }

  public Scope build() {
    return new Scope(id, calculateName(), description, subScopes,
        () -> {
          Map<String, Cell> map = new HashMap<>();
          for (Consumer<Map<String, Cell>> mutator : itemMutators) {
            mutator.accept(map);
          }
          return map;
        });
  }

}
