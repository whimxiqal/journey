package edu.whimc.journey.common.navigation;

import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * A group (set) of {@link ModeType}s.
 */
public class ModeTypeGroup implements Serializable {

  private final Set<ModeType> modeTypes = Sets.newHashSet();
  private long accumulation = 0;

  /**
   * General constructor. Resulting group has no mode types.
   */
  public ModeTypeGroup() {
  }

  /**
   * Build a mode type group with a starting sequence of modes.
   *
   * @param modes the modes
   * @param <T>   the location type
   * @param <D>   the domain type
   */
  public <T extends Locatable<T, D>, D> ModeTypeGroup(Collection<ModeType> modes) {
    modes.forEach(this::add);
  }

  /**
   * Create a mode type group from a collection of modes by
   * retrieving the types from each of them.
   *
   * @param modes the modes
   * @param <T>   the location type
   * @param <D>   the domain type
   * @return a new mode type group
   */
  public static <T extends Cell<T, D>, D> ModeTypeGroup from(Collection<Mode<T, D>> modes) {
    ModeTypeGroup modeTypeGroup = new ModeTypeGroup();
    modes.forEach(mode -> modeTypeGroup.add(mode.getType()));
    return modeTypeGroup;
  }

  /**
   * Add a mode type to the group.
   *
   * @param modeType the mode type
   * @return true if the mode type is added
   */
  public boolean add(@NotNull ModeType modeType) {
    if (this.modeTypes.add(modeType)) {
      accumulation += modeType.getAccumulationId();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Remove a mode type from the group.
   *
   * @param modeType the mode type
   * @return true if the mode type was removed
   */
  public boolean remove(@NotNull ModeType modeType) {
    if (this.modeTypes.remove(modeType)) {
      accumulation -= modeType.getAccumulationId();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Get whether a mode type is contained in this group.
   *
   * @param modeType the mode type
   * @return true if it is contained
   */
  public boolean contains(@NotNull ModeType modeType) {
    return this.modeTypes.contains(modeType);
  }

  /**
   * Return whether all the mode types in the given group are contained
   * in this mode group.
   *
   * @param modeTypeGroup the input mode type group
   * @return true if they're all contained
   */
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
