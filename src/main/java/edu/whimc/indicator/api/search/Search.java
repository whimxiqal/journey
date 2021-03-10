package edu.whimc.indicator.api.search;

import edu.whimc.indicator.api.path.Locatable;
import edu.whimc.indicator.api.path.Path;

public interface Search<T extends Locatable<T, D>, D> {

  /**
   * Find a path from an original location to a destination location.
   *
   * @param origin the starting location of a path
   * @param destination the ending location of a path
   * @return
   */
  Path<T, D> findPath(T origin, T destination);

}
