package net.whimxiqal.journey;

import java.util.Optional;
import java.util.function.Predicate;

class TunnelImpl implements Tunnel {
  
  private final Cell origin;
  private final Cell destination;
  private final int cost;
  private final Runnable prompt;
  private final Predicate<Cell> testCompletion;
  private final String permission;

  TunnelImpl(Cell origin, Cell destination, int cost, Runnable prompt,
             Predicate<Cell> testCompletion, String permission) {
    this.origin = origin;
    this.destination = destination;
    this.cost = cost;
    this.prompt = prompt;
    this.testCompletion = testCompletion;
    this.permission = permission;
  }

  @Override
  public Cell origin() {
    return origin;
  }

  @Override
  public Cell destination() {
    return destination;
  }

  @Override
  public int cost() {
    return cost;
  }

  @Override
  public void prompt() {
    if (prompt == null) {
      Tunnel.super.prompt();
      return;
    }
    prompt.run();
  }

  @Override
  public boolean testCompletion(Cell location) {
    if (testCompletion == null) {
      return Tunnel.super.testCompletion(location);
    }
    return testCompletion.test(location);
  }

  @Override
  public Optional<String> permission() {
    return Optional.ofNullable(permission);
  }
}
