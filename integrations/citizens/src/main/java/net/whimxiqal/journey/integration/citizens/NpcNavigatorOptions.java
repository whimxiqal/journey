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

package net.whimxiqal.journey.integration.citizens;

import java.util.Locale;
import java.util.stream.Collectors;
import net.whimxiqal.journey.integration.citizens.config.ConfigSettings;
import net.whimxiqal.journey.navigation.option.NavigatorOption;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;

public final class NpcNavigatorOptions {

  public static final NavigatorOption<EntityType> ENTITY_TYPE = NavigatorOption
      .builder("entity-type", EntityType.class)
      .parser(val -> Registry.ENTITY_TYPE.get(NamespacedKey.minecraft(val.toLowerCase(Locale.ENGLISH))))
      .valueSuggestions(() -> NpcNavigator.ALLOWED_GUIDE_ENTITY_TYPES.stream()
          .map(val -> val.getKey().getKey().toLowerCase(Locale.ENGLISH))
          .collect(Collectors.toList()))
      .validator((val) -> {
        if (val == null) {
          return "Entity type not found";
        } else if (!NpcNavigator.ALLOWED_GUIDE_ENTITY_TYPES.contains(val)) {
          return "That entity type is not supported";
        }
        return null;
      })
      .defaultValue(ConfigSettings.NPC_NAVIGATOR_DEFAULT_OPTIONS_ENTITY_TYPE::load)
      .permission("journey.flag.navigator.npc.entity-type")
      .valuePermission(val -> "journey.flag.navigator.npc.entity-type." + val.getKey().getKey())
      .build();
  public static final NavigatorOption<String> NAME = NavigatorOption
      .stringValueBuilder("name")
      .defaultValue(ConfigSettings.NPC_NAVIGATOR_DEFAULT_OPTIONS_NAME::load)
      .build();

  private NpcNavigatorOptions() {

  }
}
