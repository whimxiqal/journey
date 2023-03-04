/*
 * MIT License
 *
 * Copyright (c) whimxiqal
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

package net.whimxiqal.journey;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.kyori.adventure.text.Component;

class ScopeImpl implements Scope {

  private final Component name;
  private final Component description;
  private final Function<JourneyPlayer, VirtualMap<Scope>> subScopes;
  private final Function<JourneyPlayer, VirtualMap<Destination>> destinations;
  private final String permission;
  private final boolean strict;

  ScopeImpl(Component name, Component description,
            Function<JourneyPlayer, VirtualMap<Scope>> subScopes,
            Function<JourneyPlayer, VirtualMap<Destination>> destinations,
            String permission,
            boolean strict) {
    this.name = name;
    this.description = description;
    this.subScopes = subScopes;
    this.destinations = destinations;
    this.permission = permission;
    this.strict = strict;
  }

  @Override
  public Component name() {
    return name;
  }

  @Override
  public Component description() {
    return description;
  }

  @Override
  public VirtualMap<Scope> subScopes(JourneyPlayer player) {
    if (subScopes == null) {
      return Scope.super.subScopes(player);
    }
    return subScopes.apply(player);
  }

  @Override
  public VirtualMap<Destination> destinations(JourneyPlayer player) {
    if (destinations == null) {
      return Scope.super.destinations(player);
    }
    return destinations.apply(player);
  }

  @Override
  public Optional<String> permission() {
    return Optional.ofNullable(permission);
  }

  @Override
  public boolean isStrict() {
    return strict;
  }

}
