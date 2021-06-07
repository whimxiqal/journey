package edu.whimc.indicator.common.tools;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@RequiredArgsConstructor
public class BufferedFunction<T, R> implements Function<T, R> {

  private final Function<T, R> function;
  private final int delayMillis;
  private final Map<T, R> dataMap = new ConcurrentHashMap<>();
  private long latestQueryTime = 0;

  @Override
  public R apply(T value) {
    long currentTime = System.currentTimeMillis();
    R data;
    if (currentTime >= latestQueryTime + delayMillis || !dataMap.containsKey(value)) {
      latestQueryTime = currentTime;
      data = function.apply(value);
      dataMap.put(value, data);
    } else {
      data = dataMap.get(value);
    }
    return data;
  }

}
