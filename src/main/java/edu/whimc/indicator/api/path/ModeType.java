package edu.whimc.indicator.api.path;

import lombok.Data;

@Data(staticConstructor = "of")
public final class ModeType {

  public static final ModeType NONE = ModeType.of("none");  // Origin, for example

  private final String id;
}
