package edu.whimc.indicator.spigot.search.mode;

import edu.whimc.indicator.api.path.ModeType;

public final class ModeTypes {

  private ModeTypes() {
  }

  public static final ModeType WALK = ModeType.of("walk");
  public static final ModeType JUMP = ModeType.of("jump");
  public static final ModeType FLY = ModeType.of("fly");
  public static final ModeType SWIM = ModeType.of("swim");
  public static final ModeType BOAT = ModeType.of("boat");
  public static final ModeType RAIL = ModeType.of("rail");
  public static final ModeType BUILD = ModeType.of("build");

}
