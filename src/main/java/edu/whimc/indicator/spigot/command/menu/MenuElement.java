package edu.whimc.indicator.spigot.command.menu;

public abstract class MenuElement {

  private final String title;

  protected MenuElement(String title) {
    this.title = title;
  }

  abstract void onSelect();
}
