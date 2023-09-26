package net.whimxiqal.journey.search;

import java.util.LinkedList;
import java.util.List;
import net.whimxiqal.journey.Builder;

/**
 * A builder for {@link SearchFlags}s.
 */
public class SearchFlagsBuilder implements Builder<SearchFlags> {

  private final List<SearchFlag<?>> flags = new LinkedList<>();

  /**
   * Add a new {@link SearchFlag}.
   *
   * @param flag the flag
   * @return the builder, for chaining
   */
  public SearchFlagsBuilder add(SearchFlag<?> flag) {
    this.flags.add(flag);
    return this;
  }

  @Override
  public SearchFlags build() {
    return SearchFlags.of(flags);
  }
}
