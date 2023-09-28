package net.whimxiqal.journey.config.struct;

import java.lang.reflect.Type;
import net.whimxiqal.journey.config.serializer.TypeDeserializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public record ConfigFillPhase(ConfigItemType item, boolean all, boolean border, boolean top, boolean bottom) {

  public static class Deserializer extends TypeDeserializer<ConfigFillPhase> {

    @Override
    public ConfigFillPhase deserialize(Type type, ConfigurationNode node) throws SerializationException {
      return new ConfigFillPhase(node.node("item").get(ConfigItemType.class, ConfigItemType.DEFAULT),
          node.node("all").getBoolean(),
          node.node("border").getBoolean(),
          node.node("top").getBoolean(),
          node.node("bottom").getBoolean());
    }
  }

}
