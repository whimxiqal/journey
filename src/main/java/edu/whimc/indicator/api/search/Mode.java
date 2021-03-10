package edu.whimc.indicator.api.search;

import edu.whimc.indicator.api.path.Locatable;

import java.util.Set;

public interface Mode<T extends Locatable<T, D>, D> {

  Set<T> getDestinations(T origin);

}
