package net.whimxiqal.journey.bukkit.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import net.whimxiqal.journey.common.Journey;
import net.whimxiqal.journey.common.data.integration.Scope;
import net.whimxiqal.journey.common.message.Formatter;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.search.PlayerDestinationGoalSearchSession;
import net.whimxiqal.journey.common.search.PlayerSurfaceGoalSearchSession;
import net.whimxiqal.journey.common.search.SearchSession;
import net.whimxiqal.journey.common.search.flag.FlagSet;
import net.whimxiqal.mantle.common.CommandSource;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;

public class JourneyGui {

  private static final int SCOPE_ENTRIES_PER_ROW = 7;
  private static final int ROWS = 6;
  private static final int COLUMNS = 9;
  private static final List<Material> itemOptions;
  private static final List<Material> scopeOptions;

  static {
    itemOptions = new ArrayList<>(16);
    itemOptions.add(Material.BLACK_TERRACOTTA);
    itemOptions.add(Material.BLUE_TERRACOTTA);
    itemOptions.add(Material.BROWN_TERRACOTTA);
    itemOptions.add(Material.CYAN_TERRACOTTA);
    itemOptions.add(Material.GRAY_TERRACOTTA);
    itemOptions.add(Material.GREEN_TERRACOTTA);
    itemOptions.add(Material.LIGHT_BLUE_TERRACOTTA);
    itemOptions.add(Material.LIGHT_GRAY_TERRACOTTA);
    itemOptions.add(Material.LIME_TERRACOTTA);
    itemOptions.add(Material.MAGENTA_TERRACOTTA);
    itemOptions.add(Material.ORANGE_TERRACOTTA);
    itemOptions.add(Material.PINK_TERRACOTTA);
    itemOptions.add(Material.PURPLE_TERRACOTTA);
    itemOptions.add(Material.RED_TERRACOTTA);
    itemOptions.add(Material.WHITE_TERRACOTTA);
    itemOptions.add(Material.YELLOW_TERRACOTTA);

    scopeOptions = new ArrayList<>(16);
    scopeOptions.add(Material.BLACK_STAINED_GLASS_PANE);
    scopeOptions.add(Material.BLUE_STAINED_GLASS_PANE);
    scopeOptions.add(Material.BROWN_STAINED_GLASS_PANE);
    scopeOptions.add(Material.CYAN_STAINED_GLASS_PANE);
    scopeOptions.add(Material.GRAY_STAINED_GLASS_PANE);
    scopeOptions.add(Material.GREEN_STAINED_GLASS_PANE);
    scopeOptions.add(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
    scopeOptions.add(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    scopeOptions.add(Material.LIME_STAINED_GLASS_PANE);
    scopeOptions.add(Material.MAGENTA_STAINED_GLASS_PANE);
    scopeOptions.add(Material.ORANGE_STAINED_GLASS_PANE);
    scopeOptions.add(Material.PINK_STAINED_GLASS_PANE);
    scopeOptions.add(Material.PURPLE_STAINED_GLASS_PANE);
    scopeOptions.add(Material.RED_STAINED_GLASS_PANE);
    scopeOptions.add(Material.WHITE_STAINED_GLASS_PANE);
    scopeOptions.add(Material.YELLOW_STAINED_GLASS_PANE);
  }

  private final Scope scope;
  private final PaginatedGui gui;

  public JourneyGui(CommandSource source) {
    this(Scope.root(source), null);
  }

  private JourneyGui(Scope scope, JourneyGui previous) {
    this.scope = scope;
    TextComponent.Builder title = Component.text();
    title.append(Component.text("Journey To").color(Formatter.THEME));
    if (scope.name() != null) {
      title.append(Formatter.dull(" % "));
      title.append(scope.name());
    }
    this.gui = Gui.paginated()
        .title(title.build())
        .rows(ROWS)
        .create();

    if (previous != null) {
      gui.addItem(ItemBuilder.from(Material.BEDROCK)
          .name(Component.text("Back to ").append(previous.scope.name()))
          .asGuiItem(event -> {
            previous.open(event.getWhoClicked());
          }));
    }

    List<Scope> subScopes = new ArrayList<>(scope.subScopes().values());
    subScopes.sort(Comparator.comparing(Scope::id));
    for (int i = 0; i < subScopes.size(); i++) {
      Scope subScope = subScopes.get(i);
      GuiItem guiItem = ItemBuilder.from(getMaterial(subScope.id(), scopeOptions))
          .name(subScope.name())
          .lore(subScope.description())
          .asGuiItem(event -> {
            JourneyGui subScopeGui = new JourneyGui(subScope, this);
            subScopeGui.open(event.getWhoClicked());
          });
      gui.addItem(guiItem);
    }

    List<Map.Entry<String, Cell>> itemEntries = new ArrayList<>(scope.items().entrySet());
    itemEntries.sort(Map.Entry.comparingByKey());
    for (int i = 0; i < itemEntries.size(); i++) {
      Map.Entry<String, Cell> item = itemEntries.get(i);
      boolean surfaceItem = scope.id() == null && item.getKey().equals("surface");
      GuiItem guiItem = ItemBuilder.from(getMaterial(item.getKey(), itemOptions))
          .name(Formatter.accent(item.getKey()))
          .lore(surfaceItem ? Formatter.dull("Go to surface") : Formatter.cell(item.getValue()))
          .asGuiItem(event -> {
            HumanEntity player = event.getWhoClicked();
            player.closeInventory();
            SearchSession session;
            if (surfaceItem) {
              // this is a special "surface" case
              session = new PlayerSurfaceGoalSearchSession(player.getUniqueId(),
                  BukkitUtil.cell(player.getLocation()),
                   new FlagSet());
            } else {
              // normal case
              session = new PlayerDestinationGoalSearchSession(player.getUniqueId(),
                  BukkitUtil.cell(player.getLocation()),
                  item.getValue(), new FlagSet(),
                  true);
            }
            Journey.get().searchManager().launchSearch(session);
          });
      gui.addItem(guiItem);
    }
  }

  public void open(HumanEntity player) {
    gui.open(player);
  }

  private Material getMaterial(String id, List<Material> materials) {
    return materials.get(Math.abs(id.hashCode()) % materials.size());
  }

}
