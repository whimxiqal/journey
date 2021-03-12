package edu.whimc.indicator.api.journey;

import edu.whimc.indicator.api.path.Locatable;
import edu.whimc.indicator.api.path.Path;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@AllArgsConstructor
public abstract class Journey<T extends Locatable<T, D>, D> {

  @Getter private final String journeyerId;
  @Getter private final Path<T, D> path;

  abstract public void visit(T locatable);

  abstract public Collection<T> subsequent();

}
