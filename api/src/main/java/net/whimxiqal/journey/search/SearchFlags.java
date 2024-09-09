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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of {@link SearchFlag}s.
 */
public class SearchFlags {

  private final List<SearchFlag<?>> flags;

  private SearchFlags(List<SearchFlag<?>> flags) {
    this.flags = Collections.unmodifiableList(flags);
  }

  /**
   * Static constructor for a builder of {@link SearchFlags}.
   *
   * @return the builder
   */
  public static SearchFlagsBuilder builder() {
    return new SearchFlagsBuilder();
  }

  /**
   * Static constructor for an empty {@link SearchFlags}.
   *
   * @return the empty collection of flags
   */
  public static SearchFlags none() {
    return new SearchFlags(Collections.emptyList());
  }

  /**
   * Static constructor for {@link SearchFlags} using var args.
   *
   * @param flags the flags
   * @return the collection
   */
  public static SearchFlags of(SearchFlag<?>... flags) {
    return new SearchFlags(List.of(flags));
  }

  /**
   * Static constructor for {@link SearchFlags} using a {@link Collection}.
   *
   * @param flags the flags
   * @return the collection
   */
  public static SearchFlags of(Collection<SearchFlag<?>> flags) {
    return new SearchFlags(new LinkedList<>(flags));
  }

  /**
   * Get the list of {@link SearchFlag}s in this collection.
   *
   * @return the list
   */
  public List<SearchFlag<?>> get() {
    return this.flags;
  }

}
