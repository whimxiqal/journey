package net.whimxiqal.journey.navigation;

import java.util.Collection;
import net.whimxiqal.journey.Builder;
import net.whimxiqal.journey.navigation.option.Color;

public interface TrailNavigationRequestBuilder extends Builder<NavigationRequest> {

  TrailNavigationRequestBuilder particle(String particle);

  TrailNavigationRequestBuilder particles(Collection<String> particles);

  TrailNavigationRequestBuilder particles(String... particles);

  TrailNavigationRequestBuilder color(int red, int green, int blue);

  TrailNavigationRequestBuilder color(Color color);

  TrailNavigationRequestBuilder colors(Collection<Color> colors);

  TrailNavigationRequestBuilder colors(Color... colors);

  TrailNavigationRequestBuilder width(double width);

  TrailNavigationRequestBuilder density(double density);

}
