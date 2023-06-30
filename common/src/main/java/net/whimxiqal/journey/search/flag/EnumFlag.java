package net.whimxiqal.journey.search.flag;

import java.util.function.Supplier;

public class EnumFlag<E extends Enum<E>> extends Flag<E>{
  public EnumFlag(String name, Supplier<E> defaultValue, String permission, Class<E> clazz) {
    super(name, defaultValue, permission, clazz);
  }
}
