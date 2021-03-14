package edu.whimc.indicator.api.path;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Data
public final class Trail<T extends Locatable<T, D>, D> {

  private final ArrayList<Step<T, D>> steps;
  private final double length;

  public T getOrigin() {
    if (steps.isEmpty()) return null;
    return steps.get(0).getLocatable();
  }

  public T getDestination() {
    if (steps.isEmpty()) return null;
    return steps.get(steps.size() - 1).getLocatable();
  }

}
