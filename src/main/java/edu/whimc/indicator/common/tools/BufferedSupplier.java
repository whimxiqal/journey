package edu.whimc.indicator.common.tools;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * A supplier which only gets the data after a certain
 * amount of time after the previous time the data was taken
 *
 * @param <T> the type of data supplied
 */
@RequiredArgsConstructor
public class BufferedSupplier<T> implements Supplier<T> {

  private final Supplier<T> supplier;
  private final int delayMillis;
  private long latestQueryTime = 0;
  private T data;

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
