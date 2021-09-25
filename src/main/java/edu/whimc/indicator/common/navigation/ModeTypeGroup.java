package edu.whimc.indicator.common.navigation;

import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ModeTypeGroup implements Serializable {

  private final Set<ModeType> modeTypes = Sets.newHashSet();
  @Getter
  private int primeProduct = 1;

  public ModeTypeGroup() {
  }

  public <T extends Locatable<T, D>, D> ModeTypeGroup(Collection<ModeType> modes) {
    modes.forEach(this::add);
  }

  public boolean add(@NotNull ModeType modeType) {
    if (this.modeTypes.add(modeType)) {
      primeProduct *= modeType.getPrimeIdentifier();
      return true;
    } else {
      return false;
    }
  }

  public boolean remove(@NotNull ModeType modeType) {
    if (this.modeTypes.remove(modeType)) {
      primeProduct /= modeType.getPrimeIdentifier();
      return true;
    } else {
      return false;
    }
  }

  public boolean contains(@NotNull ModeType modeType) {
    return this.modeTypes.contains(modeType);
  }

  public boolean containsAll(@NotNull ModeTypeGroup modeTypeGroup) {
    return this.modeTypes.containsAll(modeTypeGroup.modeTypes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ModeTypeGroup that = (ModeTypeGroup) o;

    return this.primeProduct == that.primeProduct;
  }

  @Override
  public int hashCode() {
    return primeProduct;
  }

  @Override
  public String toString() {
    return "ModeTypeGroup{"
        + "modeTypes=" + modeTypes
        + ", primeProduct=" + primeProduct
        + '}';
  }
}
