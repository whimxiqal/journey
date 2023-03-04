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
 * The central interface for all external-facing endpoints for Journey.
 */
public interface JourneyApi {

  /**
   * Register a {@link Scope} to Journey. These will appear throughout the plugin,
   * allowing players to journey to custom locations.
   *
   * @param id    the id of the scope. Characters must be alphanumeric, a hyphen, or a space.
   * @param scope the scope
   */
  void registerScope(String plugin, String id, Scope scope);

  /**
   * Register a supplier of {@link Tunnel}s, indicating unique methods of travel through which
   * players may get to locations unnaturally.
   *
   * @param tunnelSupplier the supplier of tunnels for a given player
   */
  void registerTunnels(String plugin, TunnelSupplier tunnelSupplier);

}
