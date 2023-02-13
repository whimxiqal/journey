package net.whimxiqal.journey;

import java.util.function.Predicate;

public class TunnelBuilder implements Builder<Tunnel> {

  private final Cell origin;
  private final Cell destination;
  private int cost = Tunnel.DEFAULT_COST;
  private Runnable prompt;
  private Predicate<Cell> completionTester;
  private String permission;

  public TunnelBuilder(Cell origin, Cell destination) {
    this.origin = origin;
    this.destination = destination;
  }

  public TunnelBuilder cost(int cost) {
    this.cost = cost;
    return this;
  }

  public TunnelBuilder prompt(Runnable prompt) {
    this.prompt = prompt;
    return this;
  }

  public TunnelBuilder completionTester(Predicate<Cell> completionTester) {
    this.completionTester = completionTester;
    return this;
  }

  public TunnelBuilder permission(String permission) {
    this.permission = permission;
    return this;
  }

  @Override
  public Tunnel build() {
    return new TunnelImpl(origin, destination, cost, prompt, completionTester, permission);
  }
}
