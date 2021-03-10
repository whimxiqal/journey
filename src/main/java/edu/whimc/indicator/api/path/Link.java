package edu.whimc.indicator.api.path;

public interface Link<T extends Locatable<T, D>, D> {

  T getOrigin();

  T getDestination();

}
