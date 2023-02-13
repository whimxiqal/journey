package net.whimxiqal.journey;

import java.util.Optional;
import java.util.UUID;
import net.whimxiqal.mantle.common.CommandSource;
import net.whimxiqal.mantle.common.Mantle;

public abstract class JourneyPlayerImpl implements JourneyPlayer {

  protected final UUID uuid;
  protected final String name;

  public JourneyPlayerImpl(UUID uuid, String name) {
    this.uuid = uuid;
    this.name = name;
  }

  public static JourneyPlayer from(CommandSource source) {
    if (source.type() != CommandSource.Type.PLAYER) {
      throw new IllegalArgumentException("Can only create a JourneyPlayer from a player type CommandSource");
    }
    Optional<JourneyPlayer> optional = Journey.get().proxy().platform().onlinePlayer(source.uuid());
    if (optional.isEmpty()) {
      throw new IllegalStateException("Player " + source.uuid() + " cannot be found");
    }
    return optional.get();
  }

  @Override
  public UUID uuid() {
    return uuid;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean hasPermission(String permission) {
    // Piggyback off Mantle
    return Mantle.getProxy().hasPermission(uuid, permission);
  }

  @Override
  public String toString() {
    return name + " (" + uuid + ")";
  }
}
