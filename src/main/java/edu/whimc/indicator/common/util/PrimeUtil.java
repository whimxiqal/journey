package edu.whimc.indicator.common.util;

import com.google.common.collect.Lists;

import java.util.List;

public final class PrimeUtil {

  public static final List<Integer> PRIME_NUMBERS = Lists.newArrayList(
      2, 3, 5, 7, 11, 13, 17, 19, 23, 29,
      31, 37, 41, 43, 47, 53, 59, 61, 67, 71,
      73, 79, 83, 89, 97, 101, 103, 107, 109, 113
  );
  private int primeIndex = 0;

  public int getNextPrime() {
    if (primeIndex >= PRIME_NUMBERS.size()) {
      throw new IllegalStateException("Not enough prime numbers for the quantity of registered mode types");
    }
    return PRIME_NUMBERS.get(primeIndex++);
  }

}
