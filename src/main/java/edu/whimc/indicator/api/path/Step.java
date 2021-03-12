package edu.whimc.indicator.api.path;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Step<T extends Locatable<T, D>, D> {
  private final T locatable;
  private ModeType modeType;
}
