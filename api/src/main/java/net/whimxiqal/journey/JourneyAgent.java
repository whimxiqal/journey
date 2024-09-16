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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.whimxiqal.journey.search.ModeType;

/**
 * An entity representing the ability to move along a path throughout Minecraft worlds.
 */
public interface JourneyAgent {


  /**
   * The agent's UUID.
   *
   * @return the UUID
   */
  UUID uuid();

  /**
   * The agent's current location, if it exists.
   *
   * @return the location
   */
  @Synchronous
  Optional<Cell> location();

  /**
   * Whether the agent has a permission.
   *
   * @param permission the permission
   * @return true if the agent has the permission
   */
  @Synchronous
  boolean hasPermission(String permission);

  /**
   * The {@link Audience} with which messages and content may
   * be sent to the agent.
   *
   * @return the audience
   */
  Audience audience();

  /**
   * The capabilities that this agent has to move throughout the world.
   *
   * @return a set of all capabilities
   */
  @Synchronous
  Set<ModeType> modeCapabilities();

}
