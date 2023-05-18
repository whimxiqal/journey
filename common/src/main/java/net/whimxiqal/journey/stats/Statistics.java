package net.whimxiqal.journey.stats;

public final class Statistics {

  /**
   * Number of searches per hour
   */
  public final static UnaryRateStatistic<Integer> SEARCHES_PER_HOUR = new UnaryRateStatistic<>(0);

  /**
   * Number of blocks travelled along a path per hour
   */
  public final static RateStatistic<Double, Integer> BLOCKS_TRAVELLED_PER_HOUR = new RateStatistic<>(0.0, value -> (int) Math.floor(value));

  private Statistics() {
  }
}
