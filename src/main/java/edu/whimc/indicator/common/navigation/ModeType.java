/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.indicator.common.navigation;

import java.io.Serializable;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public enum ModeType implements Serializable {

  NONE("none", false, 1),  // Origin, for example
  WALK("walk", true, 1 << 1),
  JUMP("jump", true, 1 << 2),
  SWIM("swim", true, 1 << 3),
  FLY("fly", true, 1 << 4),
  BOAT("boat", false, 1 << 5),
  RAIL("rail", false, 1 << 6),
  BUILD("build", false, 1 << 7),
  DOOR("door", true, 1 << 8),
  CLIMB("climb", true, 1 << 9),
  NETHER_PORTAL("netherportal", false, 1 << 10),
  COMMAND("command", true, 1 << 11),
  LEAP("leap", false, 1 << 12);

  @Getter
  private final String id;

  @Getter
  private final boolean common;

  @Getter
  private final long accumulationId;

  ModeType(@NotNull String id, boolean common, long accumulationId) {
    this.id = id;
    this.common = common;
    this.accumulationId = accumulationId;
  }

  @Override
  public String toString() {
    return "ModeType{" + "id='"
        + id + '\'' + '}';
  }
}
