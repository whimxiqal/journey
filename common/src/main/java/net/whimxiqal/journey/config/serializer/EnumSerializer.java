package net.whimxiqal.journey.config.serializer;

import java.lang.reflect.Type;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

public class EnumSerializer<E extends Enum<E>> implements TypeSerializer<Enum<E>> {

  private final Class<E> clazz;

  public EnumSerializer(Class<E> clazz) {
    this.clazz = clazz;
  }

  @Override
  public Enum<E> deserialize(Type type, ConfigurationNode node) throws SerializationException {
    String value = node.getString();
    if (value == null) {
      throw new SerializationException("Value was null");
    }
    try {
      return E.valueOf(clazz, value.toUpperCase(Locale.ENGLISH));
    } catch (IllegalArgumentException e) {
      throw new SerializationException(e);
    }
  }

  @Override
  public void serialize(Type type, @Nullable Enum<E> obj, ConfigurationNode node) throws SerializationException {
    if (obj == null) {
      throw new SerializationException("Enum object was null");
    }
    node.set(String.class, obj.toString());
  }
}
