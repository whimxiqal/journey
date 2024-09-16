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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.whimxiqal.journey.Color;

public class TrailNavigatorDetailsBuilderImpl extends NavigatorDetailsBuilderImpl<TrailNavigatorDetailsBuilderImpl> implements TrailNavigatorDetailsBuilder<TrailNavigatorDetailsBuilderImpl> {

  public TrailNavigatorDetailsBuilderImpl() {
    super(TrailNavigator.TRAIL_NAVIGATOR_ID);
  }

  @Override
  protected TrailNavigatorDetailsBuilderImpl getDerived() {
    return this;
  }

  @Override
  public TrailNavigatorDetailsBuilderImpl particle(String particle) {
    options.put(TrailNavigator.OPTION_ID_PARTICLE, List.of(particle));
    return this;
  }

  @Override
  public TrailNavigatorDetailsBuilderImpl particles(Collection<String> particles) {
    options.put(TrailNavigator.OPTION_ID_PARTICLE, new ArrayList<>(particles));
    return this;
  }

  @Override
  public TrailNavigatorDetailsBuilderImpl particles(String... particles) {
    options.put(TrailNavigator.OPTION_ID_PARTICLE, Arrays.stream(particles).toList());
    return this;
  }

  @Override
  public TrailNavigatorDetailsBuilderImpl color(int red, int green, int blue) {
    options.put(TrailNavigator.OPTION_ID_COLOR, List.of(new Color(red, green, blue)));
    return this;
  }

  @Override
  public TrailNavigatorDetailsBuilderImpl color(Color color) {
    options.put(TrailNavigator.OPTION_ID_COLOR, List.of(color));
    return this;
  }

  @Override
  public TrailNavigatorDetailsBuilderImpl colors(Collection<Color> colors) {
    options.put(TrailNavigator.OPTION_ID_COLOR, new ArrayList<>(colors));
    return this;
  }

  @Override
  public TrailNavigatorDetailsBuilderImpl colors(Color... colors) {
    options.put(TrailNavigator.OPTION_ID_COLOR, Arrays.stream(colors).toList());
    return this;
  }

  @Override
  public TrailNavigatorDetailsBuilderImpl width(double width) {
    options.put(TrailNavigator.OPTION_ID_WIDTH, width);
    return this;
  }

  @Override
  public TrailNavigatorDetailsBuilderImpl density(double density) {
    options.put(TrailNavigator.OPTION_ID_DENSITY, density);
    return this;
  }

}
