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
