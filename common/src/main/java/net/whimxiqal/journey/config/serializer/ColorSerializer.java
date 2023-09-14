package net.whimxiqal.journey.config.serializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import net.whimxiqal.journey.navigation.option.Color;
import net.whimxiqal.journey.util.ColorUtil;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class ColorSerializer extends TypeDeserializer<Color> {
  @Override
  public Color deserialize(Type type, ConfigurationNode node) throws SerializationException {
    try {
      return ColorUtil.fromHex(node.getString());
    } catch (ParseException e) {
      throw new SerializationException("Could not parse hexadecimal value for " + node.path() + ": " + node.getString());
    }
  }

}
