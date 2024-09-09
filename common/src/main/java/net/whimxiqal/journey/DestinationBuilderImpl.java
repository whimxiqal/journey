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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;

/**
 * A builder for a {@link Destination}.
 */
public class DestinationBuilderImpl implements DestinationBuilder {

  private final TargetFunction targetFunction;
  private final Predicate<Cell> satisfactionPredicate;
  private final boolean stationary;
  private Component name = Component.empty();
  private List<Component> description = Collections.emptyList();
  private String permission = null;

  DestinationBuilderImpl(TargetFunction targetFunction, Predicate<Cell> satisfactionPredicate, boolean stationary) {
    this.targetFunction = targetFunction;
    this.satisfactionPredicate = satisfactionPredicate;
    this.stationary = stationary;
  }


  @Override
  public DestinationBuilder name(Component name) {
    this.name = name;
    return this;
  }

  @Override
  public DestinationBuilder description(Component... description) {
    this.description = Arrays.asList(description);
    return this;
  }

  @Override
  public DestinationBuilder description(List<Component> description) {
    this.description = description;
    return this;
  }

  @Override
  public DestinationBuilder permission(String permission) {
    this.permission = permission;
    return this;
  }

  @Override
  public Destination build() {
    return new DestinationImpl(name, description, permission, targetFunction, satisfactionPredicate, stationary);
  }
}
