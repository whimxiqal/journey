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
