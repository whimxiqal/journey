package net.whimxiqal.journey.search.flag;

import java.util.List;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyPlayer;

public class PlayerFlag extends Flag<JourneyPlayer> {
  public PlayerFlag(String name, String permission) {
    super(name, null, permission, JourneyPlayer.class);
  }

  @Override
  public String printValue(JourneyPlayer val) {
    return val.name();
  }

  @Override
  public List<? extends JourneyPlayer> suggestedValues() {
    return Journey.get().proxy().platform().onlinePlayers();
  }
}
