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
