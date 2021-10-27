package edu.whimc.journey.spigot.command.common;

import java.util.Map;
import java.util.function.BiFunction;
import org.bukkit.entity.Player;

/**
 * A command flag that represents both a key and a value.
 * The data is not stored in the value flag itself but is rather
 * retrieved from a flag set created at the time of command execution.
 *
 * @param <T> the data type of the value
 */
public class ValueFlag<T> extends CommandFlag {

  private final BiFunction<Player, String, T> parser;

  /**
   * General constructor.
   *
   * @param key    the key of the flag
   * @param parser the parser which gets the value from some data string,
   *               based on the player who called the command
   */
  public ValueFlag(String key, BiFunction<Player, String, T> parser) {
    super(key);
    this.parser = parser;
  }

  /**
   * Retrieval method of the data that this value flag represents
   * from a mapping of flags and their values taken from a command execution.
   *
   * @param player  the player who called the command
   * @param flagMap the flags directly from the command
   * @return the data retrieved from the flag map
   */
  public T retrieve(Player player, Map<String, String> flagMap) {
    String value = flagMap.get(getKey());
    if (value == null) {
      return null;
    } else {
      return parser.apply(player, value);
    }
  }

}
