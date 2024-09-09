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

public class DestinationBuilderFactoryImpl implements DestinationBuilderFactory {

  @Override
  public DestinationBuilder cellDestinationBuilder(Cell location) {
    return new DestinationBuilderImpl(new CellTargetFunction(location), location::equals, true);
  }

  @Override
  public DestinationBuilder cellDestinationBuilder(CellSupplier location) {
    return new DestinationBuilderImpl(new CellTargetFunction(location.get()),
        c -> c.equals(location.get()), // can be null from supplier
        false);
  }

  @Override
  public DestinationBuilder boxDestinationBuilder(Cell point1, Cell point2) {
    CellBox boxHelper = new CellBox(point1, point2);
    return new DestinationBuilderImpl(new CellTargetFunction(boxHelper.center()), boxHelper::contains, true);
  }

}
