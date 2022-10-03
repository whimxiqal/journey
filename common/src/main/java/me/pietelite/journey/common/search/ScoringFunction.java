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

package me.pietelite.journey.common.search;

import java.util.function.Function;
import lombok.Value;

/**
 * An interface to represent the score of a given node.
 * Of all the nodes that are currently in the running for the
 * "next best node to try" throughout this algorithm,
 * the one with the highest score is chosen next.
 */
@Value
public class ScoringFunction
    implements Function<FlexiblePathTrial.Node, Double> {
  Function<FlexiblePathTrial.Node, Double> function;
  Type type;

  @Override
  public Double apply(FlexiblePathTrial.Node node) {
    return function.apply(node);
  }

  /**
   * Get the type.
   *
   * @return the type
   */
  public Type getType() {
    return type;
  }

  /**
   * A type of scoring function.
   */
  public enum Type {
    EUCLIDEAN_DISTANCE,
    HEIGHT,
    OTHER
  }

}
