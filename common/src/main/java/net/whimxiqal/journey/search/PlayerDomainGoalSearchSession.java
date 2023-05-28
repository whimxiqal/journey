package net.whimxiqal.journey.search;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.search.flag.Flags;

public class PlayerDomainGoalSearchSession extends DomainGoalSearchSession implements PlayerSessionStateful {

  private final PlayerSessionState sessionState;

  public PlayerDomainGoalSearchSession(JourneyPlayer player, int destinationDomain) {
    this(player.uuid(), player.location(), destinationDomain);
  }

  public PlayerDomainGoalSearchSession(UUID player, Cell origin, int destinationDomain) {
    super(player, SearchSession.Caller.PLAYER, origin, destinationDomain, false);
    this.sessionState = new PlayerSessionState(player);
  }

  @Override
  public Audience audience() {
    return Journey.get().proxy().audienceProvider().player(getCallerId());
  }

  @Override
  public PlayerSessionState sessionState() {
    return sessionState;
  }

  @Override
  public void initialize() {
    int stepDelay = flags.getValueFor(Flags.ANIMATE);
    if (stepDelay > 0) {
      sessionState.animationManager().setAnimating(true);
      setAlgorithmStepDelay(stepDelay);
    } else {
      sessionState.animationManager().setAnimating(false);
    }

    SearchSession.registerPlayerModes(this, getCallerId(), flags);
    Journey.get().proxy().platform().onlinePlayer(getCallerId()).ifPresent(jPlayer ->
        Journey.get().tunnelManager().tunnels(jPlayer).forEach(this::registerTunnel));
  }
}
