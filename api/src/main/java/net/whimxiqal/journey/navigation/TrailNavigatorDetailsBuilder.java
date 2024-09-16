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

import java.util.Collection;
import net.whimxiqal.journey.Color;

/**
 * A builder for {@link NavigatorDetails} specifically for "trail navigators".
 *
 * @param <B> the derived type of builder, for more helpful chaining
 */
public interface TrailNavigatorDetailsBuilder<B extends TrailNavigatorDetailsBuilder<B>>
    extends NavigatorDetailsBuilder<B> {

  /**
   * Set the type of particle, which will be used to illuminate the path to the agent.
   *
   * @param particle the particle type
   * @return the builder, for chaining
   */
  B particle(String particle);

  /**
   * Set multiple types of particles, which will all be used to illuminate the path to the agent.
   *
   * @param particles the particle type
   * @return the builder, for chaining
   */
  B particles(Collection<String> particles);

  /**
   * Set multiple types of particles, which will all be used to illuminate the path to the agent.
   *
   * @param particles the particle type
   * @return the builder, for chaining
   */
  B particles(String... particles);

  /**
   * Set the color of the particles.
   * Colors will only appear on dust particles.
   *
   * @param red   the red component of the color
   * @param green the green component of the color
   * @param blue  the blue component of the color
   * @return the builder, for chaining
   */
  B color(int red, int green, int blue);

  /**
   * Set the color of the particles.
   * Colors will only appear on dust particles.
   *
   * @param color the color
   * @return the builder, for chaining
   */
  B color(Color color);

  /**
   * Set multiple colors of the particles. Each particle will use a random color from the collection.
   * Colors will only appear on dust particles.
   *
   * @param colors the colors
   * @return the builder, for chaining
   */
  B colors(Collection<Color> colors);

  /**
   * Set multiple colors of the particles. Each particle will use a random color from the collection.
   * Colors will only appear on redstone particles.
   *
   * @param colors the colors
   * @return the builder, for chaining
   */
  B colors(Color... colors);

  /**
   * Set the width of the trail, in blocks.
   *
   * @param width the width
   * @return the builder, for chaining
   */
  B width(double width);

  /**
   * Set the density of the trail, in blocks.
   *
   * @param density the density
   * @return the builder, for chaining
   */
  B density(double density);

}
