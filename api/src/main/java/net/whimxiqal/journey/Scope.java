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

/**
 * A nested collection of destinations.
 */
public interface Scope extends Describable, Permissible {

  /**
   * A builder for a {@link Scope}.
   *
   * @return the builder
   */
  static ScopeBuilder builder() {
    return new ScopeBuilder();
  }

  /**
   * A supplier of scopes to be accessed only under the current scope.
   *
   * @param player the player
   * @return the supplier of sub-scopes
   */
  default VirtualMap<Scope> subScopes(JourneyPlayer player) {
    return VirtualMap.empty();
  }

  /**
   * A supplier of destinations to which players may travel.
   *
   * @param player the player
   * @return the supplier
   */
  default VirtualMap<Destination> destinations(JourneyPlayer player) {
    return VirtualMap.empty();
  }

  /**
   * Whether this scope's ID must be specified to properly
   * contextualize this scope's destinations and sub-scopes.
   *
   * <p>This should be true if players generally don't need/want to know
   * about the contents of this scope, but it's still available to them if
   * they go looking for it by specifying this scope
   *
   * @return true if the contents of this scope should only ever been referenced strictly under this scope
   */
  default boolean isStrict() {
    return false;
  }

}
