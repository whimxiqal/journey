package net.whimxiqal.journey.search;

import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.tools.AlternatingList;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link SearchGraph} for finding a path to a specific domain
 */
public class DomainSearchGraph extends SearchGraph {

  private final int destinationDomain;

  public DomainSearchGraph(GraphGoalSearchSession<DomainSearchGraph> session, Cell origin, int domain) {
    super(session, origin);
    this.destinationDomain = domain;
  }

  @Nullable
  @Override
  public ItineraryTrial calculate(boolean mustUseCache) {
    AlternatingList<Tunnel, PathTrial, Object> graphPath = findMinimumPath(originNode,
        node -> node.destination().domain() == destinationDomain,
        trial -> !(mustUseCache && trial.isFromCache()));
    if (graphPath == null) {
      return null;
    } else {
      return new ItineraryTrial(session, origin, graphPath);
    }
  }

}
