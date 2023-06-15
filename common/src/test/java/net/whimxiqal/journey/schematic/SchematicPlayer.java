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

package net.whimxiqal.journey.schematic;

import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.InternalJourneyPlayer;
import net.whimxiqal.journey.util.ConsoleAudience;

public class SchematicPlayer extends InternalJourneyPlayer {

  private Cell location;
  private boolean canFly;
  private boolean hasBoat;

  public SchematicPlayer() {
    super(UUID.randomUUID(), "player");
  }

  @Override
  public Optional<Cell> location() {
    return Optional.of(location);
  }

  public void setLocation(Cell location) {
    this.location = location;
  }

  @Override
  public Audience audience() {
    return new ConsoleAudience();
  }

  @Override
  public boolean canFly() {
    return canFly;
  }

  public void setCanFly(boolean canFly) {
    this.canFly = canFly;
  }

  @Override
  public boolean hasBoat() {
    return hasBoat;
  }

  public void setHasBoat(boolean hasBoat) {
    this.hasBoat = hasBoat;
  }
}
