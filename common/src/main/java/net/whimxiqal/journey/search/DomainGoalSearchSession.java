package net.whimxiqal.journey.search;

import java.util.UUID;
import net.whimxiqal.journey.Cell;

public abstract class DomainGoalSearchSession extends GraphGoalSearchSession<DomainSearchGraph> {
  protected final int domain;

  public DomainGoalSearchSession(UUID callerId, Caller callerType, Cell origin, int destinationDomain, boolean persistentOrigin) {
    super(callerId, callerType, origin, persistentOrigin);
    this.domain = destinationDomain;
  }

  @Override
  protected void resumeSearch() {
    if (domain == origin.domain()) {
      synchronized (this) {
        state = ResultState.STOPPED_ERROR;
      }
      return;
    }
    super.resumeSearch();
  }

  @Override
  DomainSearchGraph createSearchGraph() {
    return new DomainSearchGraph(this, origin, domain);
  }

}
