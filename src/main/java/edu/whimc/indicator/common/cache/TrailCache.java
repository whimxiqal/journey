package edu.whimc.indicator.common.cache;

import com.google.common.collect.Maps;
import edu.whimc.indicator.common.path.Locatable;
import edu.whimc.indicator.common.path.Trail;
import edu.whimc.indicator.common.path.ModeTypeGroup;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrailCache<T extends Locatable<T, D>, D> {

  private final Map<T, Map<T, Map<ModeTypeGroup, Trail<T, D>>>> cache = new ConcurrentHashMap<>();

  @Nullable
  public Trail<T, D> put(T origin, T destination, ModeTypeGroup modeTypes, Trail<T, D> trail) {
    cache.putIfAbsent(origin, Maps.newHashMap());
    Map<T, Map<ModeTypeGroup, Trail<T, D>>> destinationMap = cache.get(origin);
    destinationMap.putIfAbsent(destination, Maps.newHashMap());
    Map<ModeTypeGroup, Trail<T, D>> modeTypeMap = destinationMap.get(destination);

    Trail<T, D> replaced = modeTypeMap.get(modeTypes);
    modeTypeMap.put(modeTypes, trail);
    return replaced;
  }

  @Nullable
  public Trail<T, D> get(T origin, T destination, ModeTypeGroup modeTypes) {
    Map<ModeTypeGroup, Trail<T, D>> modeTypeMap = getModeTypeMap(origin, destination);
    if (modeTypeMap != null) {
      return modeTypeMap.get(modeTypes);
    }
    return null;
  }

  @Nullable
  public Map<ModeTypeGroup, Trail<T, D>> getModeTypeMap(T origin, T destination) {
    if (cache.containsKey(origin)) {
      Map<T, Map<ModeTypeGroup, Trail<T, D>>> destinationMap = cache.get(origin);
      if (destinationMap.containsKey(destination)) {
        return destinationMap.get(destination);
      }
    }
    return null;
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
  public Trail<T, D> getAnyMatching(T origin, T destination, ModeTypeGroup modeTypeGroup) {
    Map<ModeTypeGroup, Trail<T, D>> modeTypeMap = getModeTypeMap(origin, destination);
    if (modeTypeMap == null) return null;
    for (Map.Entry<ModeTypeGroup, Trail<T, D>> mappedGroupEntry : modeTypeMap.entrySet()) {
      if (modeTypeGroup.containsAll(mappedGroupEntry.getKey())) {
        return mappedGroupEntry.getValue();
      }
    }
    return null;
  }

  public boolean contains(T origin, T destination, ModeTypeGroup modeTypes) {
    Map<ModeTypeGroup, Trail<T, D>> modeTypeMap = getModeTypeMap(origin, destination);
    if (modeTypeMap == null) {
      return false;
    }
    return modeTypeMap.containsKey(modeTypes);
  }

}
