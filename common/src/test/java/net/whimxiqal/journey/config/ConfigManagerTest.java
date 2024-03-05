package net.whimxiqal.journey.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import net.whimxiqal.journey.AssetVersion;
import net.whimxiqal.journey.Journey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {

  @Test
  void downgradeConfig() throws IOException {
    File file = File.createTempFile("config", "yml");
    Assertions.assertTrue(file.delete());
    URL resourceUrl = getClass().getClassLoader().getResource("config.yml");
    if (resourceUrl == null) {
      Assertions.fail("Couldn't get config.yml resource");
    }
    Files.copy(resourceUrl.openConnection().getInputStream(), file.toPath());
    Assertions.assertEquals(1, ConfigManager.downgradeConfig(file.toPath()));
  }

}