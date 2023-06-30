package net.whimxiqal.journey.search.flag;

public record FlagPair<T>(Flag<T> flag, T value) {

  public boolean valid() {
    return flag.valid(value);
  }

  public String printValue() {
    return flag.printValue(value);
  }

}
