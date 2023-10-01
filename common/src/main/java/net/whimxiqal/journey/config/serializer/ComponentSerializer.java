package net.whimxiqal.journey.config.serializer;

import java.lang.reflect.Type;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.message.MessageManager;
import org.spongepowered.configurate.ConfigurationNode;

public class ComponentSerializer extends TypeDeserializer<Component> {

  @Override
  public Component deserialize(Type type, ConfigurationNode node) {
    String serialized = node.getString();
    if (serialized == null) {
      return Component.empty();
    }
    return MessageManager.miniMessage().deserialize(serialized);
  }

}
