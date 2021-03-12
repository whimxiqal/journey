package edu.whimc.indicator.api.path;

import lombok.Data;

import java.util.LinkedList;

@Data
public class Trail<T extends Locatable<T, D>, D> {

  private final LinkedList<Step<T, D>> steps;
  private final double length;

}
