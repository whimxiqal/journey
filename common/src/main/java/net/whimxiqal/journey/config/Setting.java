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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import net.whimxiqal.journey.Journey;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * A setting, generally used as a key-value pair from a configuration file.
 * These are enumerated in {@link Settings} and they shouldn't be created elsewhere.
 *
 * @param <T> the data type stored in the setting
 */
public class Setting<T> {

  protected final Class<T> clazz;
  protected final String path;
  protected final String[] pathTokens;
  protected final T defaultValue;
  private final boolean reloadable;
  protected boolean deprecated;
  protected final AtomicReference<T> value;
  protected boolean initialized = false;
  protected boolean loaded = false;  // true if loaded from config, otherwise could have just been set from default

  Setting(@NotNull String path, @NotNull T defaultValue, @NotNull Class<T> clazz, boolean reloadable) {
    if (!clazz.isInstance(defaultValue)) {
      throw new IllegalArgumentException("The value must match the class type");
    }
    this.path = Objects.requireNonNull(path);
    this.pathTokens = path.split("\\.");
    this.defaultValue = Objects.requireNonNull(defaultValue);
    this.value = new AtomicReference<>(defaultValue);
    this.clazz = clazz;
    this.reloadable = reloadable;
  }

  /**
   * Return the configuration path of the setting.
   *
   * @return the path
   */
  @NotNull
  public final String getPath() {
    return path;
  }

  /**
   * Get the default value stored in this setting.
   *
   * @return the default value
   */
  @NotNull
  public final T getDefaultValue() {
    return clazz.cast(defaultValue);
  }

  /**
   * Get the value stored in this setting.
   *
   * @return the setting value
   */
  @NotNull
  public final T getValue() {
    if (!initialized) {
      throw new RuntimeException("Setting " + path + " was not initialized");
    }
    return value.get();
  }

  /**
   * Set the value for this setting.
   *
   * @param value the value
   */
  public final void setValue(@NotNull T value) {
    this.value.set(Objects.requireNonNull(value));
    this.initialized = true;
  }

  /**
   * Print the value stored in this setting into a serialized format.
   *
   * @return the string (serialized) form of the value
   */
  @NotNull
  public String printValue(T value) {
    return value.toString();
  }

  public final String printValue() {
    if (!initialized) {
      throw new RuntimeException("Setting " + path + " was not initialized");
    }
    return printValue(value.get());
  }

  public final void load(CommentedConfigurationNode root) throws SerializationException {
    CommentedConfigurationNode node = root.node((Object[]) pathTokens);
    if (node.virtual()) {
      // don't print out value if it is too long
      T def = getDefaultValue();
      String defPrinted = printValue(def);
      Journey.logger().warn(String.format("Setting %s was not set in your config file. Using default%s", path, (defPrinted.length() > 64 ? "" : ": " + defPrinted)));
      this.value.set(def);
      this.initialized = true;
      return;
    }

    if (deprecated) {
      Journey.logger().warn(String.format("Setting %s has been deprecated. Please consult the documentation on how to update", path));
    }

    T originalValue = this.value.get();
    T loadedValue = deserialize(node);
    if (!valid(loadedValue)) {
      T def = getDefaultValue();
      String defPrinted = printValue(def);
      Journey.logger().warn(String.format("Setting %s has invalid value %s. Using default%s",
          path, printValue(), (defPrinted.length() > 20 ? "" : ": " + defPrinted)));
      this.value.set(def);
      this.initialized = true;
      return;
    }

    if (this.initialized && !originalValue.equals(loadedValue) && !reloadable) {
      Journey.logger().warn(String.format("Saw setting %s was reloaded from config, but the server must be restarted to observe its effect", path));
      return;
    }

    this.value.set(loadedValue);
    this.loaded = true;
    this.initialized = true;
  }

  protected T deserialize(CommentedConfigurationNode node) throws SerializationException {
    return node.get(this.clazz, getDefaultValue());
  }

  public final boolean valid() {
    if (!initialized) {
      return false;
    }
    return valid(value.get());
  }

  public final boolean wasLoaded() {
    return loaded;
  }

  public final boolean reloadable() {
    return reloadable;
  }

  public boolean valid(T value) {
    return true;
  }

}
