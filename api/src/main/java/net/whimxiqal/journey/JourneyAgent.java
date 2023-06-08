package net.whimxiqal.journey;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.whimxiqal.journey.search.ModeType;

/**
 * An entity representing the ability to move along a path throughout Minecraft worlds.
 */
public interface JourneyAgent {


  /**
   * The agent's UUID.
   *
   * @return the UUID
   */
  UUID uuid();

  /**
   * The agent's current location, if it exists.
   *
   * @return the location
   */
  @Synchronous
  Optional<Cell> location();

  /**
   * Whether the agent has a permission.
   *
   * @param permission the permission
   * @return true if the agent has the permission
   */
  @Synchronous
  boolean hasPermission(String permission);

  /**
   * The {@link Audience} with which messages and content may
   * be sent to the agent.
   *
   * @return the audience
   */
  Audience audience();

  /**
   * The capabilities that this agent has to move throughout the world.
   *
   * @return a set of all capabilities
   */
  @Synchronous
  Set<ModeType> modeCapabilities();

}
