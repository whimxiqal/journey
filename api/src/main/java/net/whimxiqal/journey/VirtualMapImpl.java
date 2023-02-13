package net.whimxiqal.journey;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

class VirtualMapImpl<T> implements VirtualMap<T> {

  private final Supplier<Map<String, ? extends T>> supplier;
  private final int size;
  Map<String, ? extends T> value;

  VirtualMapImpl(Supplier<Map<String, ? extends T>> supplier, int size) {
    if (size < 0) {
      throw new IllegalArgumentException("A size may not be less than 0");
    }
    this.supplier = supplier;
    this.size = size;
  }

  VirtualMapImpl(Map<String, ? extends T> map) {
    this.supplier = null;
    value = map;
    this.size = value.size();
  }

  VirtualMapImpl() {
    this.supplier = Collections::emptyMap;
    this.size = 0;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Map<String, ? extends T> getAll() {
    if (value == null) {
      value = supplier.get();
    }
    return value;
  }
}
