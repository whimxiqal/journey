package edu.whimc.indicator.common.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * A function that only calculates a new value after a certain amount of time has passed.
 * This class is useful for saving time in calculations that do not need precise results
 * from a function all the time in situations where the function has a very long operation time.
 *
 * @param <T> the input type
 * @param <R> the return type
 * @see BufferedSupplier
 */
public class BufferedFunction<T, R> implements Function<T, R> {

  private final Function<T, R> function;
  private final long delayMillis;
  private final Map<T, R> dataMap = new ConcurrentHashMap<>();
  private long latestQueryTime = 0;

  /**
   * Default constructor.
   *
   * @param function    the underlying function to get new values from inputs
   * @param delayMillis the delay in milliseconds between getting new cached values
   */
  public BufferedFunction(@NotNull Function<T, R> function, long delayMillis) {
    this.function = function;
    this.delayMillis = delayMillis;
  }

  @Override
  public R apply(T input) {
    long currentTime = System.currentTimeMillis();
    R value;
    if (currentTime >= latestQueryTime + delayMillis || !dataMap.containsKey(input)) {
      latestQueryTime = currentTime;
      value = function.apply(input);
      dataMap.put(input, value);
    } else {
      value = dataMap.get(input);
    }
    return value;
  }

}
