package net.whimxiqal.journey.bukkit.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.whimxiqal.journey.Destination;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.VirtualMap;
import net.whimxiqal.journey.Scope;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.scope.ScopeUtil;
import net.whimxiqal.journey.search.PlayerDestinationGoalSearchSession;
import net.whimxiqal.journey.search.PlayerSurfaceGoalSearchSession;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.flag.FlagSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class JourneyGui {

  private static final int MAX_SCOPE_ITEM_COUNT = 1000;
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
//  Not black because black panes are used as filler items
    scopeOptions.add(Material.BLUE_STAINED_GLASS);
    scopeOptions.add(Material.BROWN_STAINED_GLASS);
    scopeOptions.add(Material.CYAN_STAINED_GLASS);
    scopeOptions.add(Material.GRAY_STAINED_GLASS);
    scopeOptions.add(Material.GREEN_STAINED_GLASS);
    scopeOptions.add(Material.LIGHT_BLUE_STAINED_GLASS);
    scopeOptions.add(Material.LIGHT_GRAY_STAINED_GLASS);
    scopeOptions.add(Material.LIME_STAINED_GLASS);
    scopeOptions.add(Material.MAGENTA_STAINED_GLASS);
    scopeOptions.add(Material.ORANGE_STAINED_GLASS);
    scopeOptions.add(Material.PINK_STAINED_GLASS);
    scopeOptions.add(Material.PURPLE_STAINED_GLASS);
    scopeOptions.add(Material.RED_STAINED_GLASS);
    scopeOptions.add(Material.WHITE_STAINED_GLASS);
    scopeOptions.add(Material.YELLOW_STAINED_GLASS);
  }

  private final JourneyPlayer player;
  private final Scope scope;
  private final JourneyGui previous;
  private final boolean root;

  public JourneyGui(JourneyPlayer player) {
    this(player, ScopeUtil.root(), null, true);
  }

  private JourneyGui(JourneyPlayer player, Scope scope, JourneyGui previous, boolean root) {
    this.player = player;
    this.scope = scope;
    this.previous = previous;
    this.root = root;
  }

  public void open() {
    TextComponent.Builder title = Component.text();
    title.append(Component.text("Journey To").color(Formatter.THEME));
    if (scope.name() != null) {
      title.append(Formatter.dull(" % "));
      title.append(scope.name());
    }
    PaginatedGui gui = Gui.paginated()
        .title(title.build())
        .rows(6)
        .create();
    GuiItem fillerItem = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).asGuiItem();
    gui.getFiller().fillTop(fillerItem);
    gui.getFiller().fillBottom(fillerItem);
    gui.setItem(6, 2, ItemBuilder.from(Material.PAPER).name(Formatter.accent("Previous")).asGuiItem(event -> gui.previous()));
    gui.setItem(6, 8, ItemBuilder.from(Material.PAPER).name(Formatter.accent("Next")).asGuiItem(event -> gui.next()));
    gui.setDefaultClickAction(event -> {
      event.setCancelled(true);
      event.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5f, 1));
    });

    if (previous != null) {
      gui.setItem(1, 1, ItemBuilder.from(Material.BEDROCK)
          // assume only the home page has an empty name
          .name(Formatter.accent("Back to ___", previous.scope.name().equals(Component.empty()) ? "Home Page" : previous.scope.name()))
          .asGuiItem(event -> {
            previous.open();
          }));
    }

    VirtualMap<Scope> subScopeSupplier = scope.subScopes(player);
    if (subScopeSupplier.size() < MAX_SCOPE_ITEM_COUNT) {
      subScopeSupplier.getAll().entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .filter(entry -> !ScopeUtil.restricted(entry.getValue(), player))
          .filter(entry -> entry.getValue().subScopes(player).size() > 0 || entry.getValue().destinations(player).size() > 0)
          .forEach(entry -> {
            ItemBuilder guiItem = ItemBuilder.from(getMaterial(entry.getKey(), scopeOptions))
                .name(entry.getValue().name());
            if (!entry.getValue().description().equals(Component.empty())) {
              guiItem.lore(entry.getValue().description());
            }
            gui.addItem(guiItem.asGuiItem(event -> {
              JourneyGui subScopeGui = new JourneyGui(player, entry.getValue(), this, false); // already loaded
              subScopeGui.open();
            }));
          });
    }

    VirtualMap<Destination> destinationSupplier = scope.destinations(player);
    if (destinationSupplier.size() < MAX_SCOPE_ITEM_COUNT) {
      destinationSupplier.getAll().entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .filter(entry -> !ScopeUtil.restricted(entry.getValue(), player))
          .forEach(entry -> {
            boolean surfaceItem = root && entry.getKey().equals("surface");
            GuiItem guiItem = ItemBuilder.from(getMaterial(entry.getKey(), itemOptions))
                .name(entry.getValue().name() == Component.empty() ? Formatter.accent(entry.getKey()) : entry.getValue().name())
                .lore(surfaceItem ? Formatter.dull("Go to surface") : entry.getValue().description())
                .asGuiItem(event -> {
                  HumanEntity human = event.getWhoClicked();
                  human.closeInventory();
                  SearchSession session;
                  if (surfaceItem) {
                    // this is a special "surface" case
                    session = new PlayerSurfaceGoalSearchSession(human.getUniqueId(),
                        BukkitUtil.cell(human.getLocation()),
                        new FlagSet());
                  } else {
                    // normal case
                    session = new PlayerDestinationGoalSearchSession(human.getUniqueId(),
                        BukkitUtil.cell(human.getLocation()),
                        entry.getValue().location(), new FlagSet(),
                        true);
                  }
                  Journey.get().searchManager().launchSearch(session);
                });
            gui.addItem(guiItem);
          });
    }
    Player bukkitPlayer = Bukkit.getPlayer(player.uuid());
    if (bukkitPlayer == null) {
      throw new IllegalStateException("Could not find player " + player + " when trying to open their GUI");
    }
    gui.open(bukkitPlayer);
  }

  private Material getMaterial(String id, List<Material> materials) {
    return materials.get(Math.abs(id.hashCode()) % materials.size());
  }

}
