package net.whimxiqal.journey.navigation;

import java.util.Collections;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * A struct for holding serialized information about a new navigator.
 *
 * @param navigatorType the type of navigator
 * @param options       the options to manipulate the behavior of the navigator
 */
public record NavigatorDetails(String navigatorType,
                               Map<String, Object> options) implements Comparable<NavigatorDetails> {

  /**
   * Static constructor with default options.
   *
   * @param navigatorType navigator type
   * @return the details
   */
  public static NavigatorDetails of(String navigatorType) {
    return new NavigatorDetails(navigatorType, Collections.emptyMap());
  }

  @Override
  public int compareTo(@NotNull NavigatorDetails o) {
    return navigatorType.compareTo(o.navigatorType);
  }
}
