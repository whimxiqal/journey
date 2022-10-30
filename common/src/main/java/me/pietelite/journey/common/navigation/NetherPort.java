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

import java.util.Objects;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.tools.Verifiable;
import org.jetbrains.annotations.NotNull;

/**
 * A port that represents a Minecraft Nether Portal.
 */
public final class NetherPort extends Port implements Verifiable {

  public static final int NETHER_PORT_COST = 8;
  private final Cell origin;
  private final Cell destination;

  /**
   * General constructor.
   *
   * @param origin      the origin of the portal
   * @param destination the destination of the portal
   */
  public NetherPort(@NotNull final Cell origin, @NotNull final Cell destination) {
    super(origin, destination, ModeType.NETHER_PORTAL, NETHER_PORT_COST);
    this.origin = origin;
    this.destination = destination;
  }

  @Override
  public boolean verify() {
    return Journey.get().netherManager().locateAll(origin, 1).size() > 0
        && Journey.get().netherManager().locateAll(destination, 1).size() > 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NetherPort that = (NetherPort) o;
    return getOrigin().equals(that.getOrigin()) && getDestination().equals(that.getDestination());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getOrigin(), getDestination());
  }

  @Override
  public String toString() {
    return "NetherPort: "
        + origin + " -> "
        + destination;
  }

}
