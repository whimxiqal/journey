package net.whimxiqal.journey.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.whimxiqal.journey.navigation.option.Color;

public class TrailNavigationRequestBuilderImpl implements TrailNavigationRequestBuilder {

  private final Map<String, Object> options = new HashMap<>();

  @Override
  public NavigationRequest build() {
    return new NavigationRequest(TrailNavigator.TRAIL_NAVIGATOR_ID, options);
  }

  @Override
  public TrailNavigationRequestBuilder particle(String particle) {
    options.put(TrailNavigator.OPTION_ID_PARTICLE, List.of(particle));
    return this;
  }

  @Override
  public TrailNavigationRequestBuilder particles(Collection<String> particles) {
    options.put(TrailNavigator.OPTION_ID_PARTICLE, new ArrayList<>(particles));
    return this;
  }

  @Override
  public TrailNavigationRequestBuilder particles(String... particles) {
    options.put(TrailNavigator.OPTION_ID_PARTICLE, Arrays.stream(particles).toList());
    return this;
  }

  @Override
  public TrailNavigationRequestBuilder color(int red, int green, int blue) {
    options.put(TrailNavigator.OPTION_ID_COLOR, List.of(new Color(red, green, blue)));
    return this;
  }

  @Override
  public TrailNavigationRequestBuilder color(Color color) {
    options.put(TrailNavigator.OPTION_ID_COLOR, List.of(color));
    return this;
  }

  @Override
  public TrailNavigationRequestBuilder colors(Collection<Color> colors) {
    options.put(TrailNavigator.OPTION_ID_COLOR, new ArrayList<>(colors));
    return this;
  }

  @Override
  public TrailNavigationRequestBuilder colors(Color... colors) {
    options.put(TrailNavigator.OPTION_ID_COLOR, Arrays.stream(colors).toList());
    return this;
  }

  @Override
  public TrailNavigationRequestBuilder width(double width) {
    options.put(TrailNavigator.OPTION_ID_WIDTH, width);
    return this;
  }

  @Override
  public TrailNavigationRequestBuilder density(double density) {
    options.put(TrailNavigator.OPTION_ID_DENSITY, density);
    return this;
  }
}
