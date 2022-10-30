package me.pietelite.journey.common.data;

import java.util.Collection;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.navigation.Port;

public interface PortDataManager {

  void addPort(ModeType type, Cell origin, Cell destination, double cost);

  Collection<Port> getPortsWithOrigin(ModeType type, Cell origin);

  Collection<Port> getPortsWithDestination(ModeType type, Cell destination);

  Collection<Port> getPorts(ModeType type);

  Collection<Port> getAllPorts();

  void removePorts(ModeType type, Cell origin, Cell destination);

  void removePorts(ModeType type);

}
