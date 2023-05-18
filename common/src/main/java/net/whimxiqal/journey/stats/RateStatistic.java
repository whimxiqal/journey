package net.whimxiqal.journey.stats;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class RateStatistic<T, S> {
  private final T startingValue;
  private final Function<T, S> converter;
  private final AtomicReference<T> accumulator = new AtomicReference<>();
  private S exposed;

  public RateStatistic(T startingValue, Function<T, S> converter) {
    this.startingValue = startingValue;
    this.converter = converter;
    accumulator.set(startingValue);
    exposed = converter.apply(startingValue);
  }

  public void store() {
    exposed = converter.apply(accumulator.getAndSet(startingValue));
  }

  public void update(UnaryOperator<T> func) {
    accumulator.getAndUpdate(func);
  }

  public S exposed() {
    return exposed;
  }
}
