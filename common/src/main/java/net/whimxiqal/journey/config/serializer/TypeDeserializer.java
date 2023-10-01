package net.whimxiqal.journey.config.serializer;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

public abstract class TypeDeserializer<T> implements TypeSerializer<T> {
  @Override
  public void serialize(AnnotatedType type, @Nullable T obj, ConfigurationNode node) throws SerializationException {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public @Nullable T emptyValue(Type specificType, ConfigurationOptions options) {
    return TypeSerializer.super.emptyValue(specificType, options);
  }

  @Override
  public @Nullable T emptyValue(AnnotatedType specificType, ConfigurationOptions options) {
    return TypeSerializer.super.emptyValue(specificType, options);
  }

  @Override
  public void serialize(Type type, @Nullable T obj, ConfigurationNode node) throws SerializationException {
    throw new UnsupportedOperationException("Unimplemented");
  }
}
