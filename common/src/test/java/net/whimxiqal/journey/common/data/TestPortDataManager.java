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

package net.whimxiqal.journey.common.data;

import java.util.Collection;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.navigation.ModeType;
import net.whimxiqal.journey.common.navigation.Port;

public class TestPortDataManager implements PortDataManager {
  @Override
  public void addPort(ModeType type, Cell origin, Cell destination, double cost) {

  }

  @Override
  public Collection<Port> getPortsWithOrigin(ModeType type, Cell origin) {
    return null;
  }

  @Override
  public Collection<Port> getPortsWithDestination(ModeType type, Cell destination) {
    return null;
  }

  @Override
  public Collection<Port> getPorts(ModeType type) {
    return null;
  }

  @Override
  public Collection<Port> getAllPorts() {
    return null;
  }

  @Override
  public void removePorts(ModeType type, Cell origin, Cell destination) {

  }

  @Override
  public void removePorts(ModeType type) {

  }
}
