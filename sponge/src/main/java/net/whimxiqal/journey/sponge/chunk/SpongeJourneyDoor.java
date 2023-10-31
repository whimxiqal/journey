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

package net.whimxiqal.journey.sponge.chunk;

import net.whimxiqal.journey.chunk.Direction;
import net.whimxiqal.journey.proxy.JourneyDoor;

public record SpongeJourneyDoor(org.spongepowered.api.util.Direction spongeDirection, boolean open,
                                boolean iron) implements JourneyDoor {

  @Override
  public Direction direction() {
    return switch (spongeDirection) {
      case EAST -> Direction.POSITIVE_X;
      case WEST -> Direction.NEGATIVE_X;
      case UP -> Direction.POSITIVE_Y;
      case DOWN -> Direction.NEGATIVE_Y;
      case SOUTH -> Direction.POSITIVE_Z;
      case NORTH -> Direction.NEGATIVE_Z;
      default -> throw new IllegalStateException("Unexpected value: " + spongeDirection);
    };
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public boolean isIron() {
    return iron;
  }
}
