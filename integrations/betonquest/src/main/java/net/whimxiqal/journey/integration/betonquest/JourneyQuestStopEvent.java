package net.whimxiqal.journey.integration.betonquest;

import net.whimxiqal.journey.JourneyApiProvider;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.api.quest.event.Event;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;

public class JourneyQuestStopEvent implements Event {

  public JourneyQuestStopEvent(Instruction instruction) {
    // has no instruction
  }

  @Override
  public void execute(Profile profile) {
    JourneyApiProvider.get().navigating().stopNavigation(profile.getPlayerUUID());
  }
}
