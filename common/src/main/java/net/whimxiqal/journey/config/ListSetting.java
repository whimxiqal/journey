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
