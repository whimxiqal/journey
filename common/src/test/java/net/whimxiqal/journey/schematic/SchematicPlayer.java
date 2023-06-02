package net.whimxiqal.journey.schematic;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.InternalJourneyPlayer;
import net.whimxiqal.journey.util.ConsoleAudience;

public class SchematicPlayer extends InternalJourneyPlayer {

  private Cell location;
  private boolean canFly;
  private boolean hasBoat;

  public SchematicPlayer() {
    super(UUID.randomUUID(), "player");
  }

  @Override
  public Cell location() {
    return location;
  }

  public void setLocation(Cell location) {
    this.location = location;
  }

  @Override
  public Audience audience() {
    return new ConsoleAudience();
  }

  @Override
  public boolean canFly() {
    return canFly;
  }

  public void setCanFly(boolean canFly) {
    this.canFly = canFly;
  }

  @Override
  public boolean hasBoat() {
    return hasBoat;
  }

  public void setHasBoat(boolean hasBoat) {
    this.hasBoat = hasBoat;
  }
}
