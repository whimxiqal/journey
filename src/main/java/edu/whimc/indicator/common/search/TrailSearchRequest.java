package edu.whimc.indicator.common.search;

import edu.whimc.indicator.common.path.Locatable;
import edu.whimc.indicator.common.path.Mode;
import lombok.Data;

import java.util.Collection;
import java.util.function.Supplier;

@Data
public class TrailSearchRequest<T extends Locatable<T, D>, D> {
  private final T origin;
  private final T destination;
  private final PathEdgeGraph.Node originNode;
  private final PathEdgeGraph.Node destinationNode;
  private final Supplier<Boolean> cancellation;
  private final boolean cacheable;
}
