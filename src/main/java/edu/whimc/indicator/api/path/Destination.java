package edu.whimc.indicator.api.path;

import lombok.Data;

@Data
public class Destination<P, T extends Locatable<T, D>, D> {

  private final P purpose;
  private final T location;

}
