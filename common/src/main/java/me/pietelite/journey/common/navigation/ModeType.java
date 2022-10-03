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

package me.pietelite.journey.common.navigation;

import java.io.Serializable;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * An enumeration of all possible mode types we allow.
 */
public enum ModeType implements Serializable {

  /**
   * We actually don't move to get here.
   */
  NONE("none", false),  // Origin, for example
  WALK("walk", true),
  JUMP("jump", true),
  SWIM("swim", true),
  FLY("fly", true),
  /**
   * Travel by boat.
   */
  BOAT("boat", false),
  /**
   * Travel by mine cart.
   */
  RAIL("rail", false),
  /**
   * Travel by building somewhere (across or upward, by creating a platform).
   */
  BUILD("build", false),
  /**
   * Travel by passing through a door.
   */
  DOOR("door", true),
  /**
   * Travel by climbing something, like a ladder or a vine.
   */
  CLIMB("climb", true),
  /**
   * Travel through a nether portal.
   */
  NETHER_PORTAL("netherportal", false),
  /**
   * Travel by typing a command.
   */
  COMMAND("command", true),
  /**
   * Travel through some generic port.
   */
  PORT("port", false),
  /**
   * Travel by walking and digging through blocks
   */
  DIG("dig", false);

  @Getter
  private final String id;

  @Getter
  private final boolean common;

  ModeType(@NotNull String id, boolean common) {
    this.id = id;
    this.common = common;
  }

  @Override
  public String toString() {
    return "ModeType{" + "id='"
        + id + '\'' + '}';
  }
}
