package edu.whimc.indicator.api.path;

import edu.whimc.indicator.api.path.Locatable;
import edu.whimc.indicator.api.path.ModeType;

import java.util.Map;

public interface Mode<T extends Locatable<T, D>, D> {

  Map<T, Float> getDestinations(T origin);

  ModeType getType();

}
