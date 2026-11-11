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

import dev.triumphteam.gui.builder.item.BaseItemBuilder;
import dev.triumphteam.gui.builder.item.NameLoreHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * An item builder that sets names and lore through Paper's native component API.
 *
 * <p>triumph-gui's default {@link dev.triumphteam.gui.builder.item.ItemBuilder} writes names and lore
 * by reflecting into CraftBukkit item meta through adventure-platform-bukkit, which does not work on
 * Paper 26.2 and later.
 */
public final class PaperItemBuilder extends BaseItemBuilder<PaperItemBuilder> {

  private static final NameLoreHandler NAME_LORE_HANDLER = new PaperNameLoreHandler();

  private PaperItemBuilder(ItemStack itemStack) {
    super(itemStack, NAME_LORE_HANDLER);
  }

  public static PaperItemBuilder from(ItemStack itemStack) {
    return new PaperItemBuilder(itemStack);
  }

  private static final class PaperNameLoreHandler implements NameLoreHandler {

    @Override
    public void name(ItemMeta meta, Component name) {
      meta.displayName(name);
    }

    @Override
    public void lore(ItemMeta meta, List<Component> lore) {
      meta.lore(lore.stream().filter(Objects::nonNull).toList());
    }

    @Override
    public void lore(ItemMeta meta, Consumer<List<Component>> lore) {
      List<Component> existing = meta.lore();
      List<Component> components = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
      lore.accept(components);
      meta.lore(components);
    }
  }
}
