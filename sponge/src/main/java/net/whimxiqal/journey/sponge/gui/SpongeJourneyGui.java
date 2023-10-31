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

package net.whimxiqal.journey.sponge.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import net.whimxiqal.journey.sponge.JourneySponge;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.registry.RegistryEntry;

public class SpongeJourneyGui {

  private static final int MAX_SCOPE_ITEM_COUNT = 1000;
  private static final int COLUMNS = 9;
  private static final Pattern SCOPE_PLACEHOLDER_PATTERN = Pattern.compile("\\{scope}");
  private static final Pattern DEFAULT_PLACEHOLDER_PATTERN = Pattern.compile("\\{default}");
  private final JourneyPlayer player;
  private final InternalScope scope;
  private final int page;
  private final SpongeJourneyGui previous;
  private final boolean editFlagOverlay;  // true if flags are being edited via the flag overlay
  private GuiItem[][] grid;

  public SpongeJourneyGui(JourneyPlayer player) {
    this(player, ScopeUtil.root(), 0, null, false);
  }

  private SpongeJourneyGui(JourneyPlayer player, InternalScope scope, int page, SpongeJourneyGui previous, boolean editFlagOverlay) {
    this.player = player;
    this.scope = scope;
    this.page = page;
    this.previous = previous;
    this.editFlagOverlay = editFlagOverlay;
  }

  private static <T> T pickRandom(List<T> items, Object determiner) {
    return items.get(Math.abs(determiner.hashCode()) % items.size());
  }

  private boolean setStaticButton(ConfigStaticButton button, @Nullable String defaultPlaceholderReplacement, Runnable onClick) {
    ItemStack.Builder builder = toItemBuilder(button.itemType());
    if (builder == null) {
      return false;
    }
    if (defaultPlaceholderReplacement != null) {
      builder.add(Keys.CUSTOM_NAME, MessageManager.miniMessage().deserialize(DEFAULT_PLACEHOLDER_PATTERN
          .matcher(button.itemType().name())
          .replaceAll(defaultPlaceholderReplacement)));
    }
    this.grid[button.row() - 1][button.column() - 1] = new GuiItem(builder.build(), onClick);
    return true;
  }

  private ItemStack.Builder toItemBuilder(ConfigItemType itemType) {
    Optional<RegistryEntry<ItemType>> type = ItemTypes.registry().findEntry(ResourceKey.minecraft(itemType.itemType()));
    if (type.isEmpty()) {
      return null;
    }
    if (!type.get().value().equals(ItemTypes.PLAYER_HEAD.get()) && !itemType.textureData().isEmpty()) {
      Journey.logger().error("Illegal item type: " + itemType + "; texture data was specified but material is not PLAYER_HEAD");
      return null;
    }
    ItemStack.Builder builder = ItemStack.builder()
        .itemType(type.get().value())
        .add(Keys.CUSTOM_NAME, MessageManager.miniMessage().deserialize(itemType.name()))
        .add(Keys.LORE, itemType.description().stream().map(MessageManager.miniMessage()::deserialize).collect(Collectors.toList()));
    if (!itemType.textureData().isEmpty()) {
      // TODO broken... so player head textures don't work
      builder.add(Keys.GAME_PROFILE, GameProfile.of(UUID.randomUUID())
          .withProperty(ProfileProperty.of("textures", itemType.textureData())));
    }
    builder.add(Keys.APPLIED_ENCHANTMENTS, itemType.enchantments().entrySet()
        .stream()
        .map(entry -> EnchantmentTypes.registry()
            .findEntry(ResourceKey.minecraft(entry.getKey()))
            .map(enchantmentTypeRegistryEntry -> Enchantment.of(enchantmentTypeRegistryEntry.value(), entry.getValue()))
            .orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toList()));
    return builder;
  }

  private ItemStack.Builder getMatchedItemType(String name, Setting<List<ConfigItemsRule>> ruleSetting) {
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
    this.grid = new GuiItem[Settings.GUI_ROWS.getValue()][COLUMNS];
    if (previous != null) {
      if (!setStaticButton(Settings.GUI_HOME_BUTTON.getValue(), Messages.GUI_SCREEN_HOME_TITLE.resolve(), () -> new SpongeJourneyGui(player).tryOpen()) ||
          !setStaticButton(Settings.GUI_BACK_BUTTON.getValue(), Messages.GUI_BUTTON_BACK_LABEL.resolve(), previous::tryOpen)) {
        return false;
      }
    }
    if (editFlagOverlay) {
      if (!setStaticButton(Settings.GUI_CLOSE_FLAG_EDITOR_BUTTON.getValue(), Messages.GUI_BUTTON_FLAG_EDITOR_CLOSE_LABEL.resolve(), () -> new SpongeJourneyGui(player, scope, 0, previous, false).tryOpen())) {
        return false;
      }
    } else {
      if (!setStaticButton(Settings.GUI_OPEN_FLAG_EDITOR_BUTTON.getValue(), Messages.GUI_BUTTON_FLAG_EDITOR_OPEN_LABEL.resolve(), () -> new SpongeJourneyGui(player, scope, 0, previous, true).tryOpen())) {
        return false;
      }
    }

    // set fillers
    for (ConfigFillPhase fillPhase : Settings.GUI_FILL.getValue()) {
      applyConfigFillPhase(fillPhase);
    }

    Optional<ServerPlayer> spongePlayer = Sponge.server().player(player.uuid());
    List<GuiItem> items = new LinkedList<>();
    if (spongePlayer.isEmpty()) {
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
        ItemStack.Builder builder = getMatchedItemType(flag.name(), Settings.GUI_CONTENT_FLAGS_RULE_LIST);
        builder.add(Keys.CUSTOM_NAME, Formatter.accent(flag.name()));
        if (preferences.contains(flag)) {
          builder.add(Keys.LORE, Collections.singletonList(Component.text(preferences.printValueFor(flag))));
        } else {
          builder.add(Keys.LORE, Collections.singletonList(Component.text(preferences.printValueFor(flag) + " (default)").color(Formatter.DULL)));
        }
        items.add(new GuiItem(builder.build(), () -> {
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
            .forEach(entry -> items.add(new GuiItem(getMatchedItemType(entry.getKey(), Settings.GUI_CONTENT_SCOPES_RULE_LIST)
                .add(Keys.CUSTOM_NAME, entry.getValue().wrappedScope().name() == Component.empty() ? Formatter.accent(entry.getKey()) : entry.getValue().wrappedScope().name())
                .add(Keys.LORE, entry.getValue().wrappedScope().description())
                .build(),
                () -> {
                  SpongeJourneyGui subScopeGui = new SpongeJourneyGui(player, entry.getValue(), 0, this, false); // already loaded
                  subScopeGui.tryOpen();
                })));
      }

      VirtualMap<SearchSession> sessionSupplier = scope.sessions(player);
      if (sessionSupplier.size() < MAX_SCOPE_ITEM_COUNT) {
        sessionSupplier.getAll().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .filter(entry -> !ScopeUtil.restricted(entry.getValue().permissions(), player))
            .forEach(entry -> items.add(new GuiItem(getMatchedItemType(entry.getKey(), Settings.GUI_CONTENT_DESTINATIONS_RULE_LIST)
                .add(Keys.CUSTOM_NAME, entry.getValue().name() == Component.empty() ? Formatter.accent(entry.getKey()) : entry.getValue().name())
                .add(Keys.LORE, entry.getValue().description())
                .build(),
                () -> {
                  Journey.get().searchManager().launchIngameSearch(entry.getValue());
                  Sponge.server().player(player.uuid()).ifPresent(ServerPlayer::closeInventory);
                })));
      }
    }

    // Page Buttons
    int itemsPerPage = 0;
    for (int i = 0; i < this.grid.length; i++) {
      for (int j = 0; j < this.grid[i].length; j++) {
        if (this.grid[i][j] != null) {
          continue;  // this is taken up by some button/filler already
        }
        if (i == Settings.GUI_NEXT_PAGE.getValue().row() && j == Settings.GUI_NEXT_PAGE.getValue().column()) {
          continue;  // this is going to be occupied by the page buttons
        }
        itemsPerPage++;
      }
    }
    int pages = items.size() / itemsPerPage + (items.size() % itemsPerPage == 0 ? 0 : 1);

    // set "next page" button
    Runnable nextPageFunction = null;
    if (pages > 1 && this.page < pages - 1) {
      nextPageFunction = () -> {
        SpongeJourneyGui nextPage = new SpongeJourneyGui(player, scope, this.page + 1, previous, editFlagOverlay);
        nextPage.tryOpen();
      };
    }
    if (!setStaticButton(Settings.GUI_NEXT_PAGE.getValue(), Messages.GUI_BUTTON_PAGE_NEXT.resolve(), nextPageFunction)) {
      return false;
    }

    // set "previous page" button
    Runnable previousPageFunction = null;
    if (this.page > 0) {
      previousPageFunction = () -> {
        SpongeJourneyGui previousPage = new SpongeJourneyGui(player, scope, this.page - 1, previous, editFlagOverlay);
        previousPage.tryOpen();
      };
    }
    if (!setStaticButton(Settings.GUI_PREVIOUS_PAGE.getValue(), Messages.GUI_BUTTON_PAGE_PREVIOUS.resolve(), previousPageFunction)) {
      return false;
    }

    // add items into other slots
    int pageCount = 0;
    Iterator<GuiItem> itemIterator = items.iterator();
    while (pageCount <= this.page && itemIterator.hasNext() && pageCount < 10000 /* fail safe */) {
      for (int i = 0; i < this.grid.length && itemIterator.hasNext(); i++) {
        for (int j = 0; j < this.grid[i].length && itemIterator.hasNext(); j++) {
          if (this.grid[i][j] != null) {
            continue;  // this is taken up by some button/filler already
          }
          if (i == Settings.GUI_NEXT_PAGE.getValue().row() && j == Settings.GUI_NEXT_PAGE.getValue().column()) {
            continue;  // this is going to be occupied by the page buttons
          }
          GuiItem item = itemIterator.next();
          if (pageCount != this.page) {
            continue;
          }
          // we are on the correct page and there are no static button here
          this.grid[i][j] = item;
        }
      }
      pageCount++;
    }

    ContainerType containerType = switch (Settings.GUI_ROWS.getValue()) {
      case 1 -> ContainerTypes.GENERIC_9X1.get();
      case 2 -> ContainerTypes.GENERIC_9X2.get();
      case 3 -> ContainerTypes.GENERIC_9X3.get();
      case 4 -> ContainerTypes.GENERIC_9X4.get();
      case 5 -> ContainerTypes.GENERIC_9X5.get();
      case 6 -> ContainerTypes.GENERIC_9X6.get();
      default -> null;
    };
    if (containerType == null) {
      Journey.logger().error("GUI Rows value is invalid: " + Settings.GUI_ROWS.getValue());
      return false;
    }
    InventoryMenu menu = InventoryMenu.of(ViewableInventory.builder()
        .type(containerType)
        .completeStructure()
        .plugin(JourneySponge.get().container())
        .build());

    int index = 0;
    for (GuiItem[] row : this.grid) {
      for (GuiItem slot : row) {
        if (slot != null) {
          menu.inventory().set(index, slot.item);
        }
        index++;
      }
    }
    menu.registerSlotClick((cause, container, slot, slotIndex, clickType) -> {
      int columns = this.grid[0].length;
      GuiItem item = this.grid[slotIndex / columns][slotIndex % columns];
      if (item == null) {
        // no item
        return false;
      }
      if (Settings.GUI_PLAY_SOUND.getValue()) {
        cause.first(ServerPlayer.class).ifPresent(p ->
            p.playSound(Sound.sound(SoundTypes.UI_BUTTON_CLICK.get().key().key(), Sound.Source.MASTER, 0.5f, 1)));
      }
      if (item.onClick == null) {
        return false;
      }
      item.onClick.run();
      return false;
    });

    menu.setTitle(title);
    menu.open(spongePlayer.get());
    return true;
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

  private void applyConfigFillPhase(ConfigFillPhase phase) {
    ItemStack.Builder builder = toItemBuilder(phase.item());
    if (builder == null) {
      return;
    }
    ItemStack item = builder.build();
    if (phase.all()) {
      for (int i = 0; i < this.grid.length; i++) {
        for (int j = 0; j < this.grid[i].length; j++) {
          if (this.grid[i][j] == null) {
            this.grid[i][j] = new GuiItem(item.copy(), null);
          }
        }
      }
      return;
    }
    if (phase.top() || phase.border()) {
      for (int j = 0; j < this.grid[0].length; j++) {
        if (this.grid[0][j] == null) {
          this.grid[0][j] = new GuiItem(item.copy(), null);  // top
        }
      }
    }
    if (phase.bottom() || phase.border()) {
      for (int j = 0; j < this.grid[this.grid.length - 1].length; j++) {
        if (this.grid[this.grid.length - 1][j] == null) {
          this.grid[this.grid.length - 1][j] = new GuiItem(item.copy(), null);  // bottom
        }
      }
    }
    if (phase.border()) {
      for (int i = 0; i < this.grid.length; i++) {
        if (this.grid[i][0] == null) {
          this.grid[i][0] = new GuiItem(item.copy(), null);  // left
        }
        if (this.grid[i][this.grid[i].length - 1] == null) {
          this.grid[i][this.grid[i].length - 1] = new GuiItem(item.copy(), null);  // right
        }
      }
    }
  }

  private record GuiItem(ItemStack item, Runnable onClick) {
  }

}
