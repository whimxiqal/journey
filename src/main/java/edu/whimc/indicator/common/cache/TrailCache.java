package edu.whimc.indicator.common.cache;

import com.google.common.collect.Maps;
import edu.whimc.indicator.common.navigation.Locatable;
import edu.whimc.indicator.common.navigation.Path;
import edu.whimc.indicator.common.navigation.ModeTypeGroup;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrailCache<T extends Locatable<T, D>, D> implements Serializable {

  private final Map<T, Map<T, Map<ModeTypeGroup, Path<T, D>>>> cache = new ConcurrentHashMap<>();
  private int size = 0;

  @Nullable
  public Path<T, D> put(T origin, T destination, ModeTypeGroup modeTypes, Path<T, D> path) {
    cache.putIfAbsent(origin, Maps.newHashMap());
    Map<T, Map<ModeTypeGroup, Path<T, D>>> destinationMap = cache.get(origin);
    destinationMap.putIfAbsent(destination, Maps.newHashMap());
    Map<ModeTypeGroup, Path<T, D>> modeTypeMap = destinationMap.get(destination);

    Path<T, D> replaced = modeTypeMap.get(modeTypes);
    modeTypeMap.put(modeTypes, path);

    if (replaced == null) {
      size++;
    }
    return replaced;
  }

  @Nullable
  public Path<T, D> get(T origin, T destination, ModeTypeGroup modeTypes) {
    Map<ModeTypeGroup, Path<T, D>> modeTypeMap = getModeTypeMap(origin, destination);
    if (modeTypeMap != null) {
      return modeTypeMap.get(modeTypes);
    }
    return null;
  }

  @Nullable
  public Map<ModeTypeGroup, Path<T, D>> getModeTypeMap(T origin, T destination) {
    if (cache.containsKey(origin)) {
      Map<T, Map<ModeTypeGroup, Path<T, D>>> destinationMap = cache.get(origin);
      if (destinationMap.containsKey(destination)) {
        return destinationMap.get(destination);
      }
    }
    return null;
  }

  public int size() {
    return size;
  }

  public void clear() {
    cache.clear();
    size = 0;
  }

  /**
   * Get a trail that matches any subgroup of the modeTypeGroup and the
   * other given inputs.
   *
   * @param origin        the origin location
   * @param destination   the destination location
   * @param modeTypeGroup the master set of mode types
   * @return any trail that matches this mode type group
   */
  @Nullable
  public Path<T, D> getAnyMatching(T origin, T destination, ModeTypeGroup modeTypeGroup) {
    Map<ModeTypeGroup, Path<T, D>> modeTypeMap = getModeTypeMap(origin, destination);
    if (modeTypeMap == null) return null;
    for (Map.Entry<ModeTypeGroup, Path<T, D>> mappedGroupEntry : modeTypeMap.entrySet()) {
      if (modeTypeGroup.containsAll(mappedGroupEntry.getKey())) {
        return mappedGroupEntry.getValue();
      }
    }
    return null;
  }

  public boolean contains(T origin, T destination, ModeTypeGroup modeTypes) {
    Map<ModeTypeGroup, Path<T, D>> modeTypeMap = getModeTypeMap(origin, destination);
    if (modeTypeMap == null) {
      return false;
    }
    return modeTypeMap.containsKey(modeTypes);
  }

}
