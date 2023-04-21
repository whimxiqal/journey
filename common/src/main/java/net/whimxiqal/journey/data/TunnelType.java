package net.whimxiqal.journey.data;

import java.util.HashMap;
import java.util.Map;

public enum TunnelType {

  NETHER(0);

  public final static Map<Integer, TunnelType> MAP = new HashMap<>();

  static {
    for (TunnelType type : TunnelType.values()) {
      MAP.put(type.id, type);
    }
  }

  final int id;

  TunnelType(int id) {
    this.id = id;
  }

  public int id() {
    return id;
  }

}
