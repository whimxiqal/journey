package me.pietelite.journey.common.data;

import java.util.Collection;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.navigation.Port;

public class TestPortDataManager implements PortDataManager {
  @Override
  public void addPort(ModeType type, Cell origin, Cell destination, double cost) {

  }

  @Override
  public Collection<Port> getPortsWithOrigin(ModeType type, Cell origin) {
    return null;
  }

  @Override
  public Collection<Port> getPortsWithDestination(ModeType type, Cell destination) {
    return null;
  }

  @Override
  public Collection<Port> getPorts(ModeType type) {
    return null;
  }

  @Override
  public Collection<Port> getAllPorts() {
    return null;
  }

  @Override
  public void removePorts(ModeType type, Cell origin, Cell destination) {

  }

  @Override
  public void removePorts(ModeType type) {

  }
}
