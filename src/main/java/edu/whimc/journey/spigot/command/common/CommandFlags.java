package edu.whimc.journey.spigot.command.common;

import edu.whimc.journey.common.config.Settings;
import edu.whimc.journey.spigot.util.Format;

/**
 * Utility class to enumerate out {@link CommandFlag}s.
 */
public final class CommandFlags {

  public static final CommandFlag NOFLY = new CommandFlag("nofly");

  public static final CommandFlag NODOOR = new CommandFlag("nodoor");

  public static final ValueFlag<Integer> ANIMATE = new ValueFlag<>("animate",
      (player, string) -> {
        if (string.isEmpty()) {
          return 10;
        } else {
          int value;
          try {
            value = Integer.parseInt(string);
          } catch (NumberFormatException e) {
            player.spigot().sendMessage(Format.error("Your value for the animate flag "
                + "must be an integer. Using 10."));
            return 10;
          }
          if (value < 0) {
            player.spigot().sendMessage(Format.error("Your value for the animate flag "
                + "must be positive. Using 10."));
            return 10;
          } else if (value > 2000) {
            player.spigot().sendMessage(Format.error("Your value for the animate flag "
                + "may not be greater than 2000. Using 10."));
            return 10;
          }
          return value;
        }
      });

  public static final ValueFlag<Integer> TIMEOUT = new ValueFlag<>("timeout",
      (player, string) -> {
        if (string.isEmpty()) {
          return Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
        } else {
          int value;
          try {
            value = Integer.parseInt(string);
          } catch (NumberFormatException e) {
            player.spigot().sendMessage(Format.error("Your value for the timeout flag "
                + "must be an integer. Using server default."));
            return Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
          }
          if (value < 0) {
            player.spigot().sendMessage(Format.error("Your value for the timeout flag "
                + "must be positive. Using server default."));
            return Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
          } else if (value > 2000) {
            player.spigot().sendMessage(Format.error("Your value for the timeout flag "
                + "may not be greater than 2000. Using server default."));
            return Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
          }
          return value;
        }
      });

  private CommandFlags() {
  }

}
