package net.whimxiqal.journey.stats;

public class UnaryRateStatistic<T> extends RateStatistic<T, T> {
  public UnaryRateStatistic(T startingValue) {
    super(startingValue, value -> value);
  }
}
