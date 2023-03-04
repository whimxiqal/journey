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

package net.whimxiqal.journey.navigation;

import java.io.Serializable;
import net.whimxiqal.journey.Tunnel;
import org.jetbrains.annotations.NotNull;

/**
 * An enumeration of all possible mode types we allow.
 */
public enum ModeType implements Serializable {

  /**
   * We actually don't move to get here.
   */
  NONE("none"),  // Origin, for example
  WALK("walk"),
  JUMP("jump"),
  SWIM("swim"),
  FLY("fly"),
  /**
   * Travel by boat.
   */
  BOAT("boat"),
  /**
   * Travel by mine cart.
   */
  RAIL("rail"),
  /**
   * Travel by building somewhere (across or upward, by creating a platform).
   */
  BUILD("build"),
  /**
   * Travel by passing through a door.
   */
  DOOR("door"),
  /**
   * Travel by climbing something, like a ladder or a vine.
   */
  CLIMB("climb"),
  /**
   * Travel by walking and digging through blocks
   */
  DIG("dig"),
  /**
   * Travel via a {@link Tunnel}
   */
  TUNNEL("tunnel");

  private final String id;

  ModeType(@NotNull String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  @Override
  public String toString() {
    return id;
  }
}
