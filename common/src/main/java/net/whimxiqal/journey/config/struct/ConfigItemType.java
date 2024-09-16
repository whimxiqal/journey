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

package net.whimxiqal.journey.config.struct;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.whimxiqal.journey.config.serializer.TypeDeserializer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public record ConfigItemType(@Nullable String name, List<String> description,
                             String itemType, Map<String, Integer> enchantments,
                             String textureData) {

  public static final ConfigItemType DEFAULT = new ConfigItemType(null,
      Collections.emptyList(),
      "air",
      Collections.emptyMap(),
      "");

  public static ConfigItemType of(String itemType) {
    return new ConfigItemType(null, Collections.emptyList(), itemType, Collections.emptyMap(), "");
  }

  public static class Deserializer extends TypeDeserializer<ConfigItemType> {
    @Override
    public ConfigItemType deserialize(Type type, ConfigurationNode node) throws SerializationException {
      String string = node.getString();
      if (string != null) {
        // we don't have a complicated item, it's just simply a material type
        return ConfigItemType.of(string);
      }
      String itemType = node.node("type").getString();
      if (itemType == null) {
        throw new SerializationException("Type is required for an item");
      }
      // complicated type, check all sub-values
      return new ConfigItemType(node.node("name").getString(),
          node.node("lore").getList(String.class),
          itemType,
          node.node("enchantments").childrenMap()
              .entrySet()
              .stream()
              .collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().getInt(1))),
          node.node("texture-data").getString(""));
    }

  }

}
