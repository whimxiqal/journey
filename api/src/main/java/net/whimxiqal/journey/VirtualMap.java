package net.whimxiqal.journey;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/**
 * A supplier of a {@link Map} which has {@link String} keys.
 * The size of the map may be very large, so the size of the map supplied may be given
 * so the map is only loaded in circumstances where the size is small enough to warrant it.
 *
 * @param <T> the value type stored in the map
 */
public interface VirtualMap<T> {

  /**
   * Static constructor with a supplier of a map.
   *
   * @param supplier the supplier
   * @param size     the size of the map, if it were to be supplied
   * @param <X>      the map value type
   * @return the map supplier
   */
  static <X> VirtualMap<X> of(Supplier<Map<String, ? extends X>> supplier, int size) {
    return new VirtualMapImpl<>(supplier, size);
  }

  /**
   * Static constructor with a loaded map.
   *
   * @param map the map
   * @param <X> the map value type
   * @return the map supplier
   */
  static <X> VirtualMap<X> of(Map<String, ? extends X> map) {
    return new VirtualMapImpl<>(map);
  }

  /**
   * Static constructor for easily supplying a single item.
   *
   * @param id   the id of the single item
   * @param item the item
   * @param <X>  the type of the item
   * @return the map supplier
   */
  static <X> VirtualMap<X> ofSingleton(String id, X item) {
    return new VirtualMapImpl<>(Collections.singletonMap(id, item));
  }

  /**
   * Static constructor for an empty map.
   *
   * @param <X> the map value type
   * @return the map supplier
   */
  static <X> VirtualMap<X> empty() {
    return new VirtualMapImpl<>(Collections.emptyMap());
  }

  /**
   * Get all entries, where the key is the identifier.
   *
   * @return all entries
   */
  Map<String, ? extends T> getAll();

  /**
   * The size of the map that this object supplies.
   *
   * <p>Sometimes, the size of map of values is too large to load or render to the user.
   * Here, the size may be calculated before the map is actually loaded, if such a function
   * makes sense for the type of data supplied.
   *
   * @return the size of the map that would be supplied
   */
  default int size() {
    return getAll().size();
  }

  /**
   * Get a value by its id. This can be overridden for cases where the entire map need not
   * be supplied in order to search for a value, such as components backed by a database.
   *
   * @param id the identifier
   * @return the value
   */
  @Nullable
  default T get(String id) {
    return getAll().get(id);
  }

}
