package edu.whimc.journey.common.data;

import edu.whimc.journey.common.navigation.Cell;

public interface DataManager<T extends Cell<T, D>, D> {

  CustomEndpointManager<T, D> getCustomEndpointManager();

  ServerEndpointManager<T, D> getServerEndpointManager();

}
