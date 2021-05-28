package edu.whimc.indicator.spigot.command.menu;

import lombok.Getter;

import java.util.ArrayList;

public class Menu extends MenuElement {

  @Getter
  private final String title;
  @Getter
  private final String header;
  private final ArrayList<MenuElement> elements;

  protected Menu(String title, String header, ArrayList<MenuElement> elements) {
    super(title);
    this.title = title;
    this.header = header;
    this.elements = elements;
  }

  public static MenuBuilder builder() {
    return new MenuBuilder();
  }

  public static final class MenuBuilder {
    private String title;
    private String header;
    private final ArrayList<MenuElement> elements = new ArrayList<>();

    public MenuBuilder setTitle(String title) {
      this.title = title;
      return this;
    }

    public MenuBuilder setHeader(String header) {
      this.header = header;
      return this;
    }

    public MenuBuilder addElement(MenuElement menuElement) {
      this.elements.add(menuElement);
      return this;
    }

    public MenuBuilder addElement(int index, MenuElement menuElement) {
      this.elements.add(index, menuElement);
      return this;
    }

    public Menu build() {
      return new Menu(title, header, elements);
    }
  }

}
