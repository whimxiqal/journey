package net.whimxiqal.journey.config.serializer;

import java.lang.reflect.Type;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.ConfigurationNode;

public class ComponentSerializer extends TypeDeserializer<Component> {

  MiniMessage miniMessage = MiniMessage.builder().tags(TagResolver.standard()).build();

  @Override
  public Component deserialize(Type type, ConfigurationNode node) {
    String serialized = node.getString();
    if (serialized == null) {
      return Component.empty();
    }
    return miniMessage.deserialize(serialized);
  }

}
