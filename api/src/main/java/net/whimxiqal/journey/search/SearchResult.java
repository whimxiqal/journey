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
  List<? extends SearchStep> path();

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
