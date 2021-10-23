package edu.whimc.indicator.common.navigation;

import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ModeTypeGroup implements Serializable {

  private final Set<ModeType> modeTypes = Sets.newHashSet();
  private long accumulation = 0;

  public ModeTypeGroup() {
  }

  public <T extends Locatable<T, D>, D> ModeTypeGroup(Collection<ModeType> modes) {
    modes.forEach(this::add);
  }

  public static <T extends Cell<T, D>, D> ModeTypeGroup from(Collection<Mode<T,D>> modes) {
    ModeTypeGroup modeTypeGroup = new ModeTypeGroup();
    modes.forEach(mode -> modeTypeGroup.add(mode.getType()));
    return modeTypeGroup;
  }

  public boolean add(@NotNull ModeType modeType) {
    if (this.modeTypes.add(modeType)) {
      accumulation += modeType.getAccumulationId();
      return true;
    } else {
      return false;
    }
  }

  public boolean remove(@NotNull ModeType modeType) {
    if (this.modeTypes.remove(modeType)) {
      accumulation -= modeType.getAccumulationId();
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
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ModeTypeGroup that = (ModeTypeGroup) o;

    return this.accumulation == that.accumulation;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.accumulation);
  }

  @Override
  public String toString() {
    return "ModeTypeGroup{"
        + "modeTypes=" + modeTypes
        + ", accumulation=" + accumulation
        + '}';
  }
}
