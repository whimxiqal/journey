package net.whimxiqal.journey.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.whimxiqal.journey.JourneyTestHarness;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SettingsTest extends JourneyTestHarness {

  @Test
  void everySettingExistsInConfig() throws IOException {
    ConfigManager configManager = new ConfigManager();
    File configFile = File.createTempFile("journey-config", ".yml");
    File messagesConfigFile = File.createTempFile("journey-messages", ".yml");
    Assertions.assertTrue(configFile.delete());
    Assertions.assertTrue(messagesConfigFile.delete());
    configManager.initialize(configFile.toPath(), messagesConfigFile.toPath());
    for (Map.Entry<String, Setting<?>> settingEntry : Settings.ALL_SETTINGS.entrySet()) {
      Assertions.assertTrue(settingEntry.getValue().valid());
      Assertions.assertTrue(settingEntry.getValue().wasLoaded(), "Setting " + settingEntry.getKey() + " was not loaded from config");
    }
  }

  @Test
  void everySettingDefaultIsValid() {
    for (Map.Entry<String, Setting<?>> settingEntry : Settings.ALL_SETTINGS.entrySet()) {
      Assertions.assertTrue(hasValidDefault(settingEntry.getValue()));
    }
  }

  <T> boolean hasValidDefault(Setting<T> setting) {
    return setting.valid(setting.defaultValue);
  }

  @Test
  void reloadableSettings() throws IOException {
    ConfigManager configManager = new ConfigManager();
    File configFile = File.createTempFile("journey-config", ".yml");
    File messagesConfigFile = File.createTempFile("journey-messages", ".yml");
    Assertions.assertTrue(configFile.delete());
    Assertions.assertTrue(messagesConfigFile.delete());
    configManager.initialize(configFile.toPath(), messagesConfigFile.toPath());

    // only works if these two are reloadable as such
    Assertions.assertFalse(Settings.MAX_SEARCHES.reloadable());
    Assertions.assertTrue(Settings.MAX_CACHED_CELLS.reloadable());

    int maxSearches = Settings.MAX_SEARCHES.getValue();
    int maxCells = Settings.MAX_CACHED_CELLS.getValue();
    List<String> newLines = new LinkedList<>();
    List<String> lines = Files.readAllLines(configFile.toPath());
    for (String line : lines) {
      // replace the settings in the config file
      String newLine = line.replace("max-searches: " + maxSearches, "max-searches: " + (maxSearches + 1));
      newLine = newLine.replace("max-cells: " + maxCells, "max-cells: " + (maxCells + 1));
      newLines.add(newLine);
    }
    Files.write(configFile.toPath(), newLines);
    configManager.load();
    Assertions.assertEquals(maxSearches, Settings.MAX_SEARCHES.getValue());  // hasn't changed
    Assertions.assertEquals(maxCells + 1, Settings.MAX_CACHED_CELLS.getValue());  // has changed
  }

}