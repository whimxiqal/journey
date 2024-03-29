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
import net.kyori.adventure.text.Component;

/**
 * A named place.
 */
public interface Destination extends Describable, Permissible {

  /**
   * Static constructor a builder.
   *
   * @param location the location
   * @return the destination builder
   */
  static DestinationBuilder builder(Cell location) {
    return new DestinationBuilder(location);
  }

  /**
   * Static constructor using just a location.
   *
   * @param location the location
   * @return the unnamed destination
   */
  static Destination of(Cell location) {
    return new DestinationImpl(Component.empty(), Collections.emptyList(), location, null);
  }

  /**
   * The physical location of the destination.
   *
   * @return the location
   */
  Cell location();

}
