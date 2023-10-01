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

package net.whimxiqal.journey.search;

import java.util.HashMap;
import java.util.Map;

/**
 * A type of transportation that allows {@link net.whimxiqal.journey.JourneyAgent}s, like players,
 * to move about a Minecraft world.
 */
public enum ModeType {

  /**
   * No movement.
   */
  NONE("none", 0),
  WALK("walk", 1),
  JUMP("jump", 2),
  SWIM("swim", 3),
  FLY("fly", 4),
  BOAT("boat", 5),
  // RAIL(6),
  // BUILD(7),
  /**
   * Moving through doors, either open or closed.
   */
  DOOR("door", 8),
  CLIMB("climb", 9),
  DIG("dig", 10),
  /**
   * Entering through {@link net.whimxiqal.journey.Tunnel}s, like teleporting or going through
   * Nether portals.
   */
  TUNNEL("tunnel", 11);

  private static final Map<Integer, ModeType> MODE_TYPE_MAP = new HashMap<>();

  static {
    for (ModeType type : values()) {
      MODE_TYPE_MAP.put(type.id, type);
    }
  }

  private final String stringId;
  private final int id;

  ModeType(String stringId, int id) {
    this.stringId = stringId;
    this.id = id;
  }

  /**
   * Get a mode type by its id, or null if no mode exists with the given id.
   *
   * @param id the id
   * @return the mode type
   */
  public static ModeType get(int id) {
    return MODE_TYPE_MAP.get(id);
  }

  /**
   * The permanent id of the mode type, which is used for serialization.
   *
   * @return the id
   */
  public int id() {
    return id;
  }

  /**
   * The permanent string id of the mode type, which can be used for identification.
   * Use {@link #id()} for a serialization id.
   *
   * @return the string id
   */
  public String stringId() {
    return stringId;
  }

  @Override
  public String toString() {
    return "ModeType{name=" + name() + ", id=" + id + '}';
  }
}
