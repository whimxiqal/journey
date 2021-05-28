package edu.whimc.indicator.spigot.command.menu;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class MenuData extends MenuElement {

  private final String label;
  private String value;

  public MenuData(String label) {
    super("Set " + label);
    this.label = label;
  }
}
