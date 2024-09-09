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

import java.util.HashSet;
import java.util.Set;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.manager.NetherManager;
import net.whimxiqal.journey.tools.Verifiable;
import org.jetbrains.annotations.NotNull;

/**
 * A tunnel that represents a Minecraft Nether Portal.
 */
public final class NetherTunnel implements Tunnel, Verifiable {

  public static final int COST = 8;
  private final Cell entrance;
  private final Cell exit;
  private final Set<Cell> allEntranceCells;

  /**
   * General constructor.
   *
   * @param entrance      the origin of the portal
   * @param exit the destination of the portal
   */
  public NetherTunnel(@NotNull final Cell entrance, @NotNull final Cell exit) {
    this.entrance = entrance;
    this.exit = exit;
    this.allEntranceCells = new HashSet<>();
    for (NetherManager.PortalGroup portalGroup : Journey.get().netherManager().locateAll(entrance, 1)) {
      allEntranceCells.addAll(portalGroup.blocks());
    }
  }

  @Override
  public boolean verify() {
    return !allEntranceCells.isEmpty();
  }

  @Override
  public String toString() {
    return "NetherTunnel: "
        + entrance + " -> "
        + exit;
  }

  @Override
  public Cell entrance() {
    return entrance;
  }

  @Override
  public Cell exit() {
    return exit;
  }

  @Override
  public int cost() {
    return COST;
  }

  @Override
  public boolean isSatisfiedBy(Cell location) {
    return allEntranceCells.contains(location);
  }
}
