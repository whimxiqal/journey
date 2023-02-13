package net.whimxiqal.journey;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;

/**
 * A player, as understood by Journey.
 */
public interface JourneyPlayer {

  /**
   * The player's UUID.
   *
   * @return the UUID
   */
  UUID uuid();

  /**
   * The player's name.
   *
   * @return the name
   */
  String name();

  /**
   * The player's current location
   *
   * @return the location
   */
  Cell location();

  /**
   * Whether a player has a permission.
   *
   * @param permission the permission
   * @return true if the player has the permission
   */
  boolean hasPermission(String permission);

  /**
   * The {@link Audience} with which messages and content may
   * be sent to the player.
   *
   * @return the audience
   */
  Audience audience();

}
