package net.whimxiqal.journey.config.struct;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.whimxiqal.journey.config.serializer.TypeDeserializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public record ConfigItemType(String name, List<String> description,
                             String itemType, Map<String, Integer> enchantments,
                             String textureData) {

  public static final ConfigItemType DEFAULT = new ConfigItemType("",
      Collections.emptyList(),
      "air",
      Collections.emptyMap(),
      "");

  public static ConfigItemType of(String itemType) {
    return new ConfigItemType("", Collections.emptyList(), itemType, Collections.emptyMap(), "");
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
      return new ConfigItemType(node.node("name").getString(""),
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
