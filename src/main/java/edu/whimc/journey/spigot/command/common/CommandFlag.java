package edu.whimc.journey.spigot.command.common;

import java.util.Map;
import lombok.Getter;

/**
 * A flag that can be passed when a Minecraft command is run. Enumerated in {@link CommandFlags}.
 */
public class CommandFlag {

  @Getter
  private final String key;

  /**
   * General constructor.
   *
   * @param key the identifier for this flag. Must be unique!
   */
  public CommandFlag(String key) {
    this.key = key;
  }

  /**
   * Determine whether a command flag is inside a map of flags.
   * The map should come from the command handling system.
   *
   * @param flagMap all flags
   * @return true if it is present
   */
  public boolean isIn(Map<String, String> flagMap) {
    return flagMap.containsKey(key);
  }

}
