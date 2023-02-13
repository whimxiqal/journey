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

package net.whimxiqal.journey.navigation;

import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.tools.Verifiable;
import org.jetbrains.annotations.NotNull;

/**
 * A tunnel that represents a Minecraft Nether Portal.
 */
public final class NetherTunnel implements Tunnel, Verifiable {

  public static final int COST = 8;
  private final Cell origin;
  private final Cell destination;

  /**
   * General constructor.
   *
   * @param origin      the origin of the portal
   * @param destination the destination of the portal
   */
  public NetherTunnel(@NotNull final Cell origin, @NotNull final Cell destination) {
    this.origin = origin;
    this.destination = destination;
  }

  @Override
  public boolean verify() {
    return Journey.get().netherManager().locateAll(origin, 1).size() > 0
        && Journey.get().netherManager().locateAll(destination, 1).size() > 0;
  }

  @Override
  public String toString() {
    return "NetherTunnel: "
        + origin + " -> "
        + destination;
  }

  @Override
  public Cell origin() {
    return origin;
  }

  @Override
  public Cell destination() {
    return destination;
  }

  @Override
  public int cost() {
    return COST;
  }

  @Override
  public boolean testCompletion(Cell location) {
    // TODO This should be "completed with" any of the portal blocks in this nether portal
    return Tunnel.super.testCompletion(location);
  }
}
