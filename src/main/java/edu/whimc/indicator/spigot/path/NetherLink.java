/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.indicator.spigot.path;

import edu.whimc.indicator.api.path.Completion;
import edu.whimc.indicator.api.path.Link;
import edu.whimc.indicator.spigot.util.NetherUtil;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class NetherLink implements Link<LocationCell, World> {

  private final LocationCell origin;
  private final LocationCell destination;
  private final Completion<LocationCell, World> completion;

  public NetherLink(@NotNull final LocationCell origin, @NotNull final LocationCell destination) {
    // TODO verify input
    this.origin = origin;
    this.destination = destination;
    this.completion = loc -> loc.distanceToSquared(origin) < 9;
  }

  @Override
  public LocationCell getOrigin() {
    return origin;
  }

  @Override
  public LocationCell getDestination() {
    return destination;
  }

  @Override
  public Completion<LocationCell, World> getCompletion() {
    return completion;
  }

  @Override
  public double weight() {
    return 16;
  }

  @Override
  public boolean verify() {
    return NetherUtil.locateAll(origin, 1).size() > 0
        && NetherUtil.locateAll(destination, 1).size() > 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NetherLink that = (NetherLink) o;
    return getOrigin().equals(that.getOrigin()) && getDestination().equals(that.getDestination());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getOrigin(), getDestination());
  }

  @Override
  public String toString() {
    return "NetherLink: "
        + origin + " -> "
        + destination;
  }
}
