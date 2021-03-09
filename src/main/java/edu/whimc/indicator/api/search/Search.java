package edu.whimc.indicator.api.search;

import java.util.List;

public interface Search<T> {

  /**
   * Find a path from an original location to a destination location.
   *
   * @param origin the starting location of a path
   * @param destination the ending location of a path
   * @return
   */
  List<T> findPath(T origin, T destination);

}
