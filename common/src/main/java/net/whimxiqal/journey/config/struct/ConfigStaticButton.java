package net.whimxiqal.journey.config.struct;

import java.lang.reflect.Type;
import net.whimxiqal.journey.config.serializer.TypeDeserializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public record ConfigStaticButton(int row, int column, ConfigItemType itemType) {

  public static class Deserializer extends TypeDeserializer<ConfigStaticButton> {
    @Override
    public ConfigStaticButton deserialize(Type type, ConfigurationNode node) throws SerializationException {
      ConfigStaticButton button = new ConfigStaticButton(node.node("row").getInt(),
          node.node("column").getInt(),
          node.node("item").get(ConfigItemType.class));
      if (button.row < 1 || button.row > 6) {
        throw new SerializationException("Invalid inventory row in config at node " + node.node("row").path().toString() + ": " + button.row);
      }
      if (button.column < 1 || button.column > 9) {
        throw new SerializationException("Invalid inventory column in config at node " + node.node("column").path().toString() + ": " + button.column);
      }
      return button;
    }
  }

}
