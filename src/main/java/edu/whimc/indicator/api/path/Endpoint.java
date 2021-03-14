package edu.whimc.indicator.api.path;

import lombok.Data;

@Data
public final class Endpoint<P, T extends Locatable<T, D>, D> {

  private final P purpose;
  private final T location;
  private final Completion<T, D> completion;

}
