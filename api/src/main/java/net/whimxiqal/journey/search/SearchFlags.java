package net.whimxiqal.journey.search;

public class SearchFlags {

  private final SearchFlag<?>[] flags;

  private SearchFlags(SearchFlag<?>[] flags) {
    this.flags = flags;
  }

  public static SearchFlags of(SearchFlag<?>... flags) {
    return new SearchFlags(flags);
  }

  public SearchFlag<?>[] get() {
    return this.flags;
  }

}
