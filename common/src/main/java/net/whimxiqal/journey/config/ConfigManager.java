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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.AssetVersion;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.config.serializer.ColorSerializer;
import net.whimxiqal.journey.config.serializer.ComponentSerializer;
import net.whimxiqal.journey.config.struct.ConfigFillPhase;
import net.whimxiqal.journey.config.struct.ConfigItemsRule;
import net.whimxiqal.journey.config.struct.ConfigItemType;
import net.whimxiqal.journey.config.struct.ConfigStaticButton;
import net.whimxiqal.journey.Color;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * An interface for determining the operation of a configuration manager.
 */
public class ConfigManager {

  private ConfigurationLoader<CommentedConfigurationNode> configurationLoader;

  public void initialize(Path path) throws IOException {
    configurationLoader = YamlConfigurationLoader.builder()
        .defaultOptions(options -> options.serializers(builder ->
            builder.register(Color.class, new ColorSerializer())
                .register(Component.class, new ComponentSerializer())
                .register(ConfigItemType.class, new ConfigItemType.Deserializer())
                .register(ConfigStaticButton.class, new ConfigStaticButton.Deserializer())
                .register(ConfigFillPhase.class, new ConfigFillPhase.Deserializer())
                .register(ConfigItemsRule.class, new ConfigItemsRule.Deserializer())
        )).indent(2)
        .nodeStyle(NodeStyle.BLOCK)
        .path(path)
        .build();
    File file = path.toFile();
    if (!file.exists()) {
      URL resourceUrl = getClass().getClassLoader().getResource("config.yml");
      if (resourceUrl == null) {
        throw new RuntimeException("Couldn't get config.yml resource");
      }
      Files.copy(resourceUrl.openConnection().getInputStream(), path);
      if (Journey.get().proxy().assetVersion() == AssetVersion.MINECRAFT_16_5) {
        downgradeConfig(path);
      }
    }
    load();
  }

  public static int downgradeConfig(Path path) throws IOException {
    List<String> newLines = new LinkedList<>();
    int skipped = 0;
    for (String line : Files.readAllLines(path)) {
      if (line.contains(" - glow")) {
        skipped++;
        continue;
      }
      newLines.add(line);
    }
    Files.write(path, newLines);
    return skipped;
  }

  /**
   * Load all config values from the config file to memory.
   */
  public void load() throws SerializationException {
    CommentedConfigurationNode root;
    try {
      root = configurationLoader.load();
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    for (Map.Entry<String, Setting<?>> entry : Settings.ALL_SETTINGS.entrySet()) {
      entry.getValue().load(root);
    }
  }

}
