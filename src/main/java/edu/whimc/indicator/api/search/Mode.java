package edu.whimc.indicator.api.search;

import java.util.Set;

public interface Mode<T extends Locatable<T, D>, D> {

  Set<T> getDestinations(T origin);

}
