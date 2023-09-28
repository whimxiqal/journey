package net.whimxiqal.journey.config;

import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class ListSetting<T> extends Setting<List<T>> {

  private final Class<T> subtypeClass;

  @SuppressWarnings("unchecked")
  protected ListSetting(@NotNull String path, @NotNull List<T> defaultValue, @NotNull Class<T> clazz, boolean reloadable) {
    super(path, defaultValue, (Class<List<T>>)(Object)List.class, reloadable);
    subtypeClass = clazz;
  }

  @Override
  public List<T> deserialize(CommentedConfigurationNode node) throws SerializationException {
    return node.getList(subtypeClass);
  }

  @Override
  public final @NotNull String printValue(List<T> value) {
    return "[" + value.stream().map(this::printSubtypeValue).collect(Collectors.joining(", ")) + "]";
  }

  protected @NotNull String printSubtypeValue(T value) {
    return value.toString();
  }

  @Override
  public final boolean valid(List<T> value) {
    return value.stream().allMatch(this::validSubtype);
  }

  protected boolean validSubtype(T value) {
    return true;
  }
}
