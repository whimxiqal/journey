package edu.whimc.indicator.api.path;

import lombok.Getter;

public abstract class Cell<T extends Cell<T, D>, D> implements Locatable<T, D> {

  @Getter protected final int x;
  @Getter protected final int y;
  @Getter protected final int z;
  protected final D domain;

  public Cell(int x, int y, int z, D domain) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.domain = domain;
  }

  @Override
  abstract public double distanceTo(T other);

  @Override
  public D getDomain() {
    return this.domain;
  }

  @Override
  abstract public String print();
}
