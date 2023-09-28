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

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.VirtualMap;
import net.whimxiqal.journey.config.Setting;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.config.struct.ConfigFillPhase;
import net.whimxiqal.journey.config.struct.ConfigItemType;
import net.whimxiqal.journey.config.struct.ConfigItemsRule;
import net.whimxiqal.journey.config.struct.ConfigStaticButton;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.message.MessageManager;
import net.whimxiqal.journey.message.Messages;
import net.whimxiqal.journey.scope.ScopeUtil;
import net.whimxiqal.journey.search.InternalScope;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.flag.Flag;
import net.whimxiqal.journey.search.flag.FlagSet;
import net.whimxiqal.journey.search.flag.Flags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

public class JourneyGui {

  private static final int MAX_SCOPE_ITEM_COUNT = 1000;
  private static final Pattern SCOPE_PLACEHOLDER_PATTERN = Pattern.compile("\\{scope}");
  private static final Pattern DEFAULT_PLACEHOLDER_PATTERN = Pattern.compile("\\{default}");
  private final JourneyPlayer player;
  private final InternalScope scope;
  private final JourneyGui previous;
  private final boolean editFlagOverlay;  // true if flags are being edited via the flag overlay

  public JourneyGui(JourneyPlayer player) {
    this(player, ScopeUtil.root(), null, false);
  }

  private JourneyGui(JourneyPlayer player, InternalScope scope, JourneyGui previous, boolean editFlagOverlay) {
    this.player = player;
    this.scope = scope;
    this.previous = previous;
    this.editFlagOverlay = editFlagOverlay;
  }

  private static <T> T pickRandom(List<T> items, Object determiner) {
    return items.get(Math.abs(determiner.hashCode()) % items.size());
  }

  private boolean setStaticButton(BaseGui gui, ConfigStaticButton button, @Nullable String defaultPlaceholderReplacement, GuiAction<InventoryClickEvent> clickEvent) {
    ItemBuilder builder = toItemBuilder(button.itemType());
    if (builder == null) {
      return false;
    }
    if (defaultPlaceholderReplacement != null) {
      builder.name(MessageManager.miniMessage().deserialize(DEFAULT_PLACEHOLDER_PATTERN
          .matcher(button.itemType().name())
          .replaceAll(defaultPlaceholderReplacement)));
    }
    gui.setItem(button.row(), button.column(), builder.asGuiItem(clickEvent));
    return true;
  }

  private ItemBuilder toItemBuilder(ConfigItemType itemType) {
    Material material = Registry.MATERIAL.get(NamespacedKey.minecraft(itemType.itemType()));
    if (material == null) {
      Journey.logger().error("Illegal material type in config: " + itemType.itemType());
      return null;
    }
    if (material != Material.PLAYER_HEAD && !itemType.textureData().isEmpty()) {
      Journey.logger().error("Illegal item type: " + itemType + "; texture data was specified but material is not PLAYER_HEAD");
      return null;
    }
    ItemStack stack = new ItemStack(material);
    if (!itemType.textureData().isEmpty()) {
      ItemMeta meta = stack.getItemMeta();
      if (!(meta instanceof SkullMeta skullMeta)) {
        throw new RuntimeException();
      }
      PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
      profile.setProperty(new ProfileProperty("textures", itemType.textureData()));
      skullMeta.setPlayerProfile(profile);
      stack.setItemMeta(meta);
    }
    for (Map.Entry<String, Integer> entry : itemType.enchantments().entrySet()) {
      Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(entry.getKey()));
      if (enchantment == null) {
        Journey.logger().error("Illegal enchantment type in config: " + entry.getKey());
        continue;
      }
      stack.addUnsafeEnchantment(enchantment, entry.getValue());
    }
    return ItemBuilder.from(stack)
        .name(MessageManager.miniMessage().deserialize(itemType.name()))
        .lore(itemType.description().stream().map(MessageManager.miniMessage()::deserialize).collect(Collectors.toList()));
  }

  private ItemBuilder getMatchedItemType(String name, Setting<List<ConfigItemsRule>> ruleSetting) {
    List<ConfigItemType> items = null;
    for (ConfigItemsRule rule : ruleSetting.getValue()) {
      if (rule.ruleMatcher().test(name)) {
        items = rule.items();
        break;
      }
    }
    if (items == null) {
      Journey.logger().info("'" + name + "' does not match any rules in config setting " + ruleSetting.getPath() + ". Using default rules");
      for (ConfigItemsRule rule : ruleSetting.getDefaultValue()) {
        if (rule.ruleMatcher().test(name)) {
          items = rule.items();
          break;
        }
      }
      if (items == null) {
        throw new RuntimeException("Default rules in value for " + Settings.GUI_CONTENT_FLAGS_RULE_LIST.getPath() + " did not match name " + name);
      }
    }
    return toItemBuilder(pickRandom(items, name));
  }

  public void tryOpen() {
    boolean opened = open();
    if (!opened) {
      Messages.COMMAND_GUI_ERROR.sendTo(player.audience(), Formatter.ERROR);
    }
  }

  public boolean open() {
    Component title;
    if (editFlagOverlay) {
      // Flag Preferences Editor
      title = MessageManager.miniMessage().deserialize(DEFAULT_PLACEHOLDER_PATTERN
          .matcher(Settings.GUI_FLAG_PREFERENCES_SCREEN_TITLE.getValue())
          .replaceAll((Messages.GUI_SCREEN_FLAG_EDITOR_TITLE.resolve())));
    } else if (previous == null) {
      // Home screen
      title = MessageManager.miniMessage().deserialize(DEFAULT_PLACEHOLDER_PATTERN
          .matcher(Settings.GUI_HOME_SCREEN_TITLE.getValue())
          .replaceAll(Messages.GUI_SCREEN_HOME_TITLE.resolve()));
    } else {
      // Scope screen
      title = MessageManager.miniMessage().deserialize(Settings.GUI_SCOPE_SCREEN_TITLE.getValue())
          .replaceText(TextReplacementConfig.builder()
              .match(SCOPE_PLACEHOLDER_PATTERN)
              .replacement(scope.wrappedScope().name())
              .build());
    }
    PaginatedGui gui = Gui.paginated()
        .title(title)
        .rows(Settings.GUI_ROWS.getValue())
        .create();
    if (previous != null) {
      if (!setStaticButton(gui, Settings.GUI_HOME_BUTTON.getValue(), Messages.GUI_SCREEN_HOME_TITLE.resolve(), event -> new JourneyGui(player).tryOpen()) ||
          !setStaticButton(gui, Settings.GUI_BACK_BUTTON.getValue(), Messages.GUI_BUTTON_BACK_LABEL.resolve(), event -> previous.tryOpen())) {
        return false;
      }
    }
    if (editFlagOverlay) {
      if (!setStaticButton(gui, Settings.GUI_CLOSE_FLAG_EDITOR_BUTTON.getValue(), Messages.GUI_BUTTON_FLAG_EDITOR_CLOSE_LABEL.resolve(), event -> new JourneyGui(player, scope, previous, false).tryOpen())) {
        return false;
      }
    } else {
      if (!setStaticButton(gui, Settings.GUI_OPEN_FLAG_EDITOR_BUTTON.getValue(), Messages.GUI_BUTTON_FLAG_EDITOR_OPEN_LABEL.resolve(), event -> new JourneyGui(player, scope, previous, true).tryOpen())) {
        return false;
      }
    }

    // set fillers
    for (ConfigFillPhase fillPhase : Settings.GUI_FILL.getValue()) {
      ItemBuilder builder = toItemBuilder(fillPhase.item());
      if (builder == null) {
        continue;
      }
      GuiItem item = builder.asGuiItem();
      if (fillPhase.all()) {
        gui.getFiller().fill(item);
        continue;
      }
      if (fillPhase.border()) {
        gui.getFiller().fillBorder(item);
        continue;
      }
      if (fillPhase.top()) {
        gui.getFiller().fillTop(item);
      }
      if (fillPhase.bottom()) {
        gui.getFiller().fillBottom(item);
      }
    }

    gui.setDefaultClickAction(event -> {
      event.setCancelled(true);
      if (Settings.GUI_PLAY_SOUND.getValue()) {
        event.getWhoClicked().playSound(Sound.sound(org.bukkit.Sound.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5f, 1));
      }
    });

    Player bukkitPlayer = Bukkit.getPlayer(player.uuid());
    if (bukkitPlayer == null) {
      Journey.logger().error("Could not find player " + player + " when trying to open their GUI");
      return false;
    }
    if (editFlagOverlay) {
      // populate with flag settings
      List<Flag<?>> allFlags = Flags.ALL_FLAGS.stream()
          .sorted(Comparator.comparing(Flag::name))
          .filter(flag -> player.hasPermission(flag.permission()))
          .toList();
      FlagSet preferences = Journey.get().searchManager().getFlagPreferences(player.uuid(), false);
      for (Flag<?> flag : allFlags) {
        ItemBuilder builder = getMatchedItemType(flag.name(), Settings.GUI_CONTENT_FLAGS_RULE_LIST);
        builder.name(Formatter.accent(flag.name()));
        if (preferences.contains(flag)) {
          builder.lore(Component.text(preferences.printValueFor(flag)));
        } else {
          builder.lore(Component.text(preferences.printValueFor(flag) + " (default)").color(Formatter.DULL));
        }
        gui.addItem(builder.asGuiItem(event -> {
          incrementBySuggestedValues(flag);
          this.tryOpen();
        }));
      }
    } else {
      VirtualMap<InternalScope> subScopeSupplier = scope.subScopes(player);
      if (subScopeSupplier.size() < MAX_SCOPE_ITEM_COUNT) {
        subScopeSupplier.getAll().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .filter(entry -> !ScopeUtil.restricted(entry.getValue().allPermissions(), player))
            .filter(entry -> entry.getValue().hasAnyDescendantSessions(player))
            .forEach(entry -> gui.addItem(getMatchedItemType(entry.getKey(), Settings.GUI_CONTENT_SCOPES_RULE_LIST)
                .name(entry.getValue().wrappedScope().name() == Component.empty() ? Formatter.accent(entry.getKey()) : entry.getValue().wrappedScope().name())
                .lore(entry.getValue().wrappedScope().description())
                .asGuiItem(event -> {
                  JourneyGui subScopeGui = new JourneyGui(player, entry.getValue(), this, false); // already loaded
                  subScopeGui.tryOpen();
                })));
      }

      VirtualMap<SearchSession> sessionSupplier = scope.sessions(player);
      if (sessionSupplier.size() < MAX_SCOPE_ITEM_COUNT) {
        sessionSupplier.getAll().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .filter(entry -> !ScopeUtil.restricted(entry.getValue().permissions(), player))
            .forEach(entry -> gui.addItem(getMatchedItemType(entry.getKey(), Settings.GUI_CONTENT_DESTINATIONS_RULE_LIST)
                .name(entry.getValue().name() == Component.empty() ? Formatter.accent(entry.getKey()) : entry.getValue().name())
                .lore(entry.getValue().description())
                .asGuiItem(event -> {
                  Journey.get().searchManager().launchIngameSearch(entry.getValue());
                  event.getWhoClicked().closeInventory();
                })));
      }
    }
    gui.open(bukkitPlayer);
    updatePageButtons(gui);
    return true;
  }

  private void updatePageButtons(PaginatedGui gui) {
    int pages = gui.getPagesNum();
    int currentPage = gui.getCurrentPageNum();
    if (pages <= 1) {
      return;  // don't set any buttons
    }
    if (currentPage < pages - 1) {
      if (!setStaticButton(gui, Settings.GUI_NEXT_PAGE.getValue(), Messages.GUI_BUTTON_PAGE_NEXT.resolve(), event -> {
        gui.next();
        updatePageButtons(gui);
      })) {
        return;
      }
      gui.update();
    }
    if (currentPage > 0) {
      if (!setStaticButton(gui, Settings.GUI_PREVIOUS_PAGE.getValue(), Messages.GUI_BUTTON_PAGE_PREVIOUS.resolve(), event -> {
        gui.previous();
        updatePageButtons(gui);
      })) {
        return;
      }
      gui.update();
    }
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

}
