package net.whimxiqal.journey.search;

import java.util.List;

/**
 * The result of a path search.
 */
public interface SearchResult {

  /**
   * The status of the result.
   *
   * @return the status
   */
  Status status();

  /**
   * Gets the steps of the path, but only if the search was successful.
   *
   * @return the steps of the path, or null if the search wasn't successful
   */
  List<SearchStep> path();

  /**
   * The status of a result of a search.
   */
  enum Status {
    /**
     * A path was successfully calculated.
     */
    SUCCESS,
    /**
     * A path was not calculated because it couldn't be found. The path may be impossible,
     * or the search hit a memory or time restriction.
     */
    FAILED,
    /**
     * The search was manually canceled.
     */
    CANCELED,
    /**
     * An internal error occurred during the execution of the search.
     */
    ERROR
  }

}
