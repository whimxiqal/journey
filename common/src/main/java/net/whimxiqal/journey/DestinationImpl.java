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

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

class DestinationImpl implements Destination {

  private final Component name;
  private final List<Component> description;
  private final String permission;
  private final TargetFunction targetFunction;
  private final Predicate<Cell> satisfactionPredicate;
  private final boolean stationary;

  DestinationImpl(Component name, List<Component> description, String permission,
                  TargetFunction targetFunction, Predicate<Cell> satisfactionPredicate,
                  boolean stationary) {
    this.name = name;
    this.description = description;
    this.permission = permission;
    this.targetFunction = targetFunction;
    this.satisfactionPredicate = satisfactionPredicate;
    this.stationary = stationary;
  }

  @Override
  public Component name() {
    return name;
  }

  @Override
  public List<Component> description() {
    return description;
  }

  @Override
  public Optional<String> permission() {
    return Optional.ofNullable(permission);
  }

  @Override
  public @Nullable Cell target(Cell origin) {
    return targetFunction.target(origin);
  }

  @Override
  public int domain() {
    return targetFunction.domain();
  }

  @Override
  public boolean isSatisfiedBy(Cell location) {
    return satisfactionPredicate.test(location);
  }

  @Override
  public boolean stationary() {
    return stationary;
  }
}
