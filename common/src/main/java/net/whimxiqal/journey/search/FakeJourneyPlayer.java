package net.whimxiqal.journey.search;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyPlayer;

public class FakeJourneyPlayer implements JourneyPlayer {
  @Override
  public UUID uuid() {
    return Journey.JOURNEY_CALLER;
  }

  @Override
  public String name() {
    return "JourneyRuntime";
  }

  @Override
  public Cell location() {
    return new Cell(0, 0, 0, 0);
  }

  @Override
  public boolean hasPermission(String permission) {
    return true;
  }

  @Override
  public Audience audience() {
    return Journey.get().proxy().audienceProvider().console();
  }

  @Override
  public void createItineraryTo(Cell origin, Cell destination) {}
}
