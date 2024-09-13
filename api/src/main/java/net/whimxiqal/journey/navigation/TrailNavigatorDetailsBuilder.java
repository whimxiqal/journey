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
