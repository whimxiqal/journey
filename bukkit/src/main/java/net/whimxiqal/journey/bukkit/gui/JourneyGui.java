/*
 * MIT License
 *
 * Copyright (c) whimxiqal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.whimxiqal.journey.bukkit.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.VirtualMap;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.scope.ScopeUtil;
import net.whimxiqal.journey.search.InternalScope;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.flag.Flag;
import net.whimxiqal.journey.search.flag.FlagSet;
import net.whimxiqal.journey.search.flag.Flags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class JourneyGui {

  private static final int MAX_SCOPE_ITEM_COUNT = 1000;
  private static final List<Material> itemOptions;
  private static final List<Material> scopeOptions;

  private static final int ROWS = 6;
  private static final int COLUMNS = 9;

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
  private final InternalScope scope;
  private final JourneyGui previous;
  private final boolean editFlagOverlay;  // true if flags are being edited via the flag overlay
  private final int rows;

  public JourneyGui(JourneyPlayer player) {
    this(player, ScopeUtil.root(), null, false);
  }

  private JourneyGui(JourneyPlayer player, InternalScope scope, JourneyGui previous, boolean editFlagOverlay) {
    this.player = player;
    this.scope = scope;
    this.previous = previous;
    this.editFlagOverlay = editFlagOverlay;
    this.rows = ROWS;
  }

  public void open() {
    TextComponent.Builder title = Component.text();
    title.append(Formatter.prefix());
    if (scope.wrappedScope().name() == null) {
      title.append(Component.text("Home"));
    } else {
      title.append(scope.wrappedScope().name());
    }
    PaginatedGui gui = Gui.paginated()
        .title(title.build())
        .rows(this.rows)
        .create();
    if (previous != null) {
      gui.setItem(1, 1, ItemBuilder.from(DecorativeHead.HOUSE.toItemStack())
          .name(Formatter.accent("Home"))
          .asGuiItem(event -> new JourneyGui(player).open()));
      gui.setItem(1, 2, ItemBuilder.from(Material.DARK_OAK_BOAT)
          .name(Formatter.accent("Back"))
          .asGuiItem(event -> previous.open()));
    }
    GuiItem fillerItem;
    if (editFlagOverlay) {
      gui.setItem(1, COLUMNS, ItemBuilder.from(DecorativeHead.GRAY_CHEST.toItemStack())
          .name(Formatter.accent("Close Flag Editor"))
          .asGuiItem(event -> new JourneyGui(player, scope, previous, false).open()));
      fillerItem = ItemBuilder.from(Material.PURPLE_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem();
    } else {
      gui.setItem(1, COLUMNS, ItemBuilder.from(DecorativeHead.PURPLE_CHEST.toItemStack())
          .name(Formatter.accent("Open Flag Editor"))
          .asGuiItem(event -> new JourneyGui(player, scope, previous, true).open()));
      fillerItem = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem();
    }
    gui.setItem(this.rows, 2, ItemBuilder.from(Material.PAPER).name(Formatter.accent("Previous Page")).asGuiItem(event -> gui.previous()));
    gui.setItem(this.rows, COLUMNS - 1, ItemBuilder.from(Material.PAPER).name(Formatter.accent("Next Page")).asGuiItem(event -> gui.next()));

    gui.getFiller().fillTop(fillerItem);
    gui.getFiller().fillBottom(fillerItem);

    gui.setDefaultClickAction(event -> {
      event.setCancelled(true);
      event.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5f, 1));
    });

    Player bukkitPlayer = Bukkit.getPlayer(player.uuid());
    if (bukkitPlayer == null) {
      throw new IllegalStateException("Could not find player " + player + " when trying to open their GUI");
    }
    if (editFlagOverlay) {
      // populate with flag settings
      List<Flag<?>> allFlags = Flags.ALL_FLAGS.stream()
          .sorted(Comparator.comparing(Flag::name))
          .filter(flag -> player.hasPermission(flag.permission()))
          .toList();
      FlagSet preferences = Journey.get().searchManager().getFlagPreferences(player.uuid(), false);
      for (Flag<?> flag : allFlags) {
        ItemBuilder guiItem = ItemBuilder.from(getMaterial(flag.name(), scopeOptions))
            .name(Formatter.accent(flag.name()));
        if (preferences.contains(flag)) {
          guiItem.lore(Component.text(preferences.printValueFor(flag)));
        } else {
          guiItem.lore(Component.text(preferences.printValueFor(flag) + " (default)").color(Formatter.DULL));
        }
        gui.addItem(guiItem.asGuiItem(event -> {
          incrementBySuggestedValues(flag);
          this.open();
        }));
      }
    } else {
      VirtualMap<InternalScope> subScopeSupplier = scope.subScopes(player);
      if (subScopeSupplier.size() < MAX_SCOPE_ITEM_COUNT) {
        subScopeSupplier.getAll().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .filter(entry -> !ScopeUtil.restricted(entry.getValue().allPermissions(), player))
            .filter(entry -> entry.getValue().subScopes(player).size() > 0 || entry.getValue().sessions(player).size() > 0)
            .forEach(entry -> {
              ItemBuilder guiItem = ItemBuilder.from(getMaterial(entry.getKey(), scopeOptions))
                  .name(entry.getValue().wrappedScope().name() == Component.empty() ? Formatter.accent(entry.getKey()) : entry.getValue().wrappedScope().name())
                  .lore(entry.getValue().wrappedScope().description());
              gui.addItem(guiItem.asGuiItem(event -> {
                JourneyGui subScopeGui = new JourneyGui(player, entry.getValue(), this, false); // already loaded
                subScopeGui.open();
              }));
            });
      }

      VirtualMap<SearchSession> sessionSupplier = scope.sessions(player);
      if (sessionSupplier.size() < MAX_SCOPE_ITEM_COUNT) {
        sessionSupplier.getAll().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .filter(entry -> !ScopeUtil.restricted(entry.getValue().permissions(), player))
            .forEach(entry -> {
              GuiItem guiItem = ItemBuilder.from(getMaterial(entry.getKey(), itemOptions))
                  .name(entry.getValue().name() == Component.empty() ? Formatter.accent(entry.getKey()) : entry.getValue().name())
                  .lore(entry.getValue().description())
                  .asGuiItem(event -> {
                    Journey.get().searchManager().launchIngameSearch(entry.getValue());
                    event.getWhoClicked().closeInventory();
                  });
              gui.addItem(guiItem);
            });
      }
    }
    gui.open(bukkitPlayer);
  }

  private <T> void incrementBySuggestedValues(Flag<T> flag) {
    List<T> suggestedValues = new ArrayList<>(flag.suggestedValues());
    if (suggestedValues.isEmpty()) {
      return;
    }
    suggestedValues.sort(null);

    FlagSet flagSet = Journey.get().searchManager().getFlagPreferences(player.uuid(), true);
    T currentValue = flagSet.getValueFor(flag);
    int index = Collections.binarySearch(suggestedValues, currentValue, null);
    T newValue;
    if (index >= suggestedValues.size()) {
      // our index is too far ahead, so we have to start over
      newValue = suggestedValues.get(0);
    } else if (suggestedValues.get(index).equals(currentValue)) {
      // we found our current value in the suggested values. Choose the next one
      if (index == suggestedValues.size() - 1) {
        // the next one would be out of bounds, so start over
        newValue = suggestedValues.get(0);
      } else {
        newValue = suggestedValues.get(index + 1);
      }
    } else {
      newValue = suggestedValues.get(index);
    }
    flagSet.addFlag(flag, newValue);
  }

  private Material getMaterial(String id, List<Material> materials) {
    return materials.get(Math.abs(id.hashCode()) % materials.size());
  }

}
