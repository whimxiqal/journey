package edu.whimc.indicator.spigot.quests;

import edu.whimc.indicator.spigot.command.menu.Menu;
import edu.whimc.indicator.spigot.command.menu.MenuElement;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class QuestsMenu extends Menu {
  private QuestsMenu(ArrayList<MenuElement> elements) {
    super("Quests Menu", "Destinations associated with your current quests", elements);
  }

  public static QuestsMenu buildFor(Player player) {
    // TODO create ArrayList
    ArrayList<MenuElement> elements = new ArrayList<>();

    return new QuestsMenu(elements);
  }
}
