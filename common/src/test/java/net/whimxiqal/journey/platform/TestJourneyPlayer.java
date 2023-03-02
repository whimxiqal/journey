package net.whimxiqal.journey.platform;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.JourneyPlayerImpl;

public class TestJourneyPlayer extends JourneyPlayerImpl {
  public static Cell LOCATION = new Cell(0, 0, 0, WorldLoader.domain(0));
  public TestJourneyPlayer(UUID uuid) {
    super(uuid, uuid.toString());
  }

  @Override
  public Cell location() {
    return LOCATION;
  }

  @Override
  public Audience audience() {
    return Audience.empty();
  }
}
