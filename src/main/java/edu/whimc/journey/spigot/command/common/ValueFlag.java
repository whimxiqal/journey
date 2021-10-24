package edu.whimc.journey.spigot.command.common;

import java.util.Map;
import java.util.function.BiFunction;
import org.bukkit.entity.Player;

public class ValueFlag<T> extends CommandFlag {

  private final BiFunction<Player, String, T> parser;

  public ValueFlag(String key, BiFunction<Player, String, T> parser) {
    super(key);
    this.parser = parser;
  }

  public T retrieve(Player player, Map<String, String> flagMap) {
    String value = flagMap.get(getKey());
    if (value == null) {
      return null;
    } else {
      return parser.apply(player, value);
    }
  }

}
