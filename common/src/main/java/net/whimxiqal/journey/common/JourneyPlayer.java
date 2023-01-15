package net.whimxiqal.journey.common;

import java.util.UUID;
import net.whimxiqal.journey.common.navigation.Cell;

public abstract class JourneyPlayer {

  protected final UUID uuid;
  protected final String name;

  public JourneyPlayer(UUID uuid, String name) {
    this.uuid = uuid;
    this.name = name;
  }

  public UUID uuid() {
    return uuid;
  }

  public String name() {
    return name;
  }

  public abstract Cell location();

}
