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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

class TunnelImpl implements Tunnel {

  private final Cell origin;
  private final Cell destination;
  private final int cost;
  private final Runnable prompt;
  private final Predicate<Cell> testCompletion;
  private final String permission;

  TunnelImpl(Cell origin, Cell destination, int cost, Runnable prompt,
             Predicate<Cell> testCompletion, String permission) {
    this.origin = origin;
    this.destination = destination;
    this.cost = cost;
    this.prompt = prompt;
    this.testCompletion = testCompletion;
    this.permission = permission;
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
    return cost;
  }

  @Override
  public void prompt() {
    if (prompt == null) {
      Tunnel.super.prompt();
      return;
    }
    prompt.run();
  }

  @Override
  public boolean testCompletion(Cell location) {
    if (testCompletion == null) {
      return Tunnel.super.testCompletion(location);
    }
    return testCompletion.test(location);
  }

  @Override
  public Optional<String> permission() {
    return Optional.ofNullable(permission);
  }

  @Override
  public String toString() {
    return "TunnelImpl{" + origin + " -> " + destination + ", cost=" + cost + ", permission=" + permission + '}';
  }
}
