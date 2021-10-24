package edu.whimc.journey.common.tools;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Supplier} that only calculates a new value after a certain amount of time has passed.
 * This class is useful for saving time in calculations that do not need precise results
 * from a supplier all the time in situations where the supplier has a very long operation time.
 *
 * @param <T> the input type
 * @see BufferedFunction
 */
public class BufferedSupplier<T> implements Supplier<T> {

  private final Supplier<T> supplier;
  private final long delayMillis;
  private long latestQueryTime = 0;
  private T data;

  /**
   * Default constructor.
   *
   * @param supplier    the underlying supplier to get new values from inputs
   * @param delayMillis the delay in milliseconds between getting new cached values
   */
  public BufferedSupplier(@NotNull Supplier<T> supplier, long delayMillis) {
    this.supplier = supplier;
    this.delayMillis = delayMillis;
  }

  @Override
  public T get() {
    long currentTime = System.currentTimeMillis();
    if (currentTime >= latestQueryTime + delayMillis) {
      latestQueryTime = currentTime;
      data = supplier.get();
    }
    return data;
  }

}
