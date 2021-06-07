package edu.whimc.indicator.common.data;

import edu.whimc.indicator.common.path.Cell;

public interface DataManager<T extends Cell<T, D>, D> {

  CustomEndpointManager<T, D> getCustomEndpointManager();

  ServerEndpointManager<T, D> getServerEndpointManager();

}
