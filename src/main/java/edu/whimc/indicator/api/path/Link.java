package edu.whimc.indicator.api.path;

import edu.whimc.indicator.spigot.path.NetherLink;

public interface Link<T extends Locatable<T, D>, D> {

  T getOrigin();

  T getDestination();

  Completion<T, D> getCompletion();

  default boolean isReverse(Object o) {
    if (this == o) return false;
    if (o == null || getClass() != o.getClass()) return false;
    NetherLink that = (NetherLink) o;
    return getOrigin().equals(that.getDestination()) && getDestination().equals(that.getOrigin());
  }

}
