package net.whimxiqal.journey.platform;

import java.util.UUID;
import net.whimxiqal.journey.common.JourneyPlayer;
import net.whimxiqal.journey.common.navigation.Cell;

public class TestJourneyPlayer extends JourneyPlayer {
  public TestJourneyPlayer(UUID uuid) {
    super(uuid, uuid.toString());
  }

  @Override
  public Cell location() {
    return new Cell(0, 0, 0, WorldLoader.worldResources[0]);
  }
}
