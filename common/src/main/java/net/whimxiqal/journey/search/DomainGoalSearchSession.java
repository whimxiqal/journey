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
  public void asyncSearch() {
    // Do an initial check to make sure we're not in the given domain, then run the normal execution
    if (domain == origin.domain()) {
      synchronized (this) {
        state = ResultState.STOPPED_ERROR;
        complete();
      }
      return;
    }
    super.asyncSearch();
  }

  @Override
  DomainSearchGraph createSearchGraph() {
    return new DomainSearchGraph(this, origin, domain);
  }

}
