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

import java.util.UUID;
import net.kyori.adventure.audience.Audience;

/**
 * A player, as understood by Journey.
 */
public interface JourneyPlayer {

  /**
   * The player's UUID.
   *
   * @return the UUID
   */
  UUID uuid();

  /**
   * The player's name.
   *
   * @return the name
   */
  String name();

  /**
   * The player's current location.
   *
   * @return the location
   */
  Cell location();

  /**
   * Whether a player has a permission.
   *
   * @param permission the permission
   * @return true if the player has the permission
   */
  boolean hasPermission(String permission);

  /**
   * The {@link Audience} with which messages and content may
   * be sent to the player.
   *
   * @return the audience
   */
  Audience audience();

  /**
   * Creates an itinerary for this journey player to the given {@link Cell}
   * @param origin The starting point of the itinerary.
   * @param destination The ending point of the itinerary.
   */
  void createItineraryTo(Cell origin, Cell destination);
}
