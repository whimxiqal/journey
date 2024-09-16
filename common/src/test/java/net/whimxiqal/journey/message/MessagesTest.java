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

package net.whimxiqal.journey.message;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import net.whimxiqal.journey.config.LocaleSetting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.yaml.internal.snakeyaml.Yaml;

public class MessagesTest {

  @Test
  void messagesFormatted() throws IOException, URISyntaxException {
    for (Locale locale : LocaleSetting.ALLOWED_LOCALES) {
      String resourceFileName = "messages_" + locale.getLanguage() + (locale.getCountry().isEmpty() ? "" : "_" + locale.getCountry()) + ".properties";
      URL url = getClass().getClassLoader().getResource(resourceFileName);
      Assertions.assertNotNull(url, "Could not get resource for " + resourceFileName);
      String previousKey = null;
      for (String line : Files.readAllLines(Path.of(url.toURI()), StandardCharsets.UTF_8)) {
        String[] tokens = line.split("=");
        String key = tokens[0];
        if (previousKey == null) {
          previousKey = key;
          continue;
        }
        if (previousKey.startsWith(key) || key.startsWith(previousKey)) {
          // if either starts with the other, we don't really care about ordering
          continue;
        }
        if (key.compareTo(previousKey) < 0) {
          Assertions.fail("Message file for locale " + locale.toLanguageTag() + " is not formatted correctly. "
              + "Key " + key + " should go before " + previousKey + ". "
              + "Run scripts/format-message-files.py to reformat");
        }
        previousKey = key;
      }
    }
  }

  private List<String> getEnglishKeys() {
    ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
    List<String> englishKeys = new LinkedList<>();
    for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements(); ) {
      englishKeys.add(keys.nextElement());
    }
    return englishKeys;
  }

  @Test
  void allMessagesFilesComplete() {
    List<String> englishKeys = getEnglishKeys();
    for (Locale locale : LocaleSetting.ALLOWED_LOCALES) {
      if (locale.equals(Locale.ENGLISH)) {
        continue;
      }
      ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
      Set<String> keySet = new HashSet<>();
      for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements(); ) {
        keySet.add(keys.nextElement());
      }
      for (String englishKey : englishKeys) {
        Assertions.assertTrue(keySet.contains(englishKey), "Messages file for locale " + locale.toLanguageTag()
            + " did not contain required key " + englishKey);
      }
    }
  }

  @Test
  void allMessagesInJava() throws IllegalAccessException {
    Set<String> keySet = new HashSet<>();
    for (Field field : Messages.class.getDeclaredFields()) {
      if (!MessageTemplate.class.isAssignableFrom(field.getType())) {
        continue;
      }
      MessageTemplate template = (MessageTemplate) field.get(null);
      keySet.add(template.key());
    }

    for (String key : getEnglishKeys()) {
      Assertions.assertTrue(keySet.contains(key), "Messages template java file is missing a template for key "
          + key + ". Run scripts/format-message-files.py to generate and format them all");
    }
  }

  @Test
  void correctTemplateTypes() throws IllegalAccessException {
    for (Locale locale : LocaleSetting.ALLOWED_LOCALES) {
      ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
      Map<String, String> messageMap = new HashMap<>();
      for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements(); ) {
        String key = keys.nextElement();
        messageMap.put(key, bundle.getString(key));
      }
      for (Field field : Messages.class.getDeclaredFields()) {
        if (!MessageTemplate.class.isAssignableFrom(field.getType())) {
          continue;
        }
        MessageTemplate template = (MessageTemplate) field.get(null);
        if (!messageMap.containsKey(template.key())) {
          Assertions.fail("Message file for locale " + locale.toLanguageTag() + " does not contain entry for key " + template.key());
        }
        Assertions.assertEquals(messageMap.get(template.key()).split("\\{[0-9]}", -1).length - 1, template.numElements(),
            "Locale " + locale.toLanguageTag() + ": template.numElements() did not return the number of '{[0-9]}' appeared in the template for key " + template.key());
      }
    }
  }

  void assertMessageConfigLinesContainsKey(List<String> path, int index, Object obj) {
    if (!(obj instanceof Map<?, ?>)) {
      Assertions.fail("Obj " + obj + " is not a map: @ " + path.subList(0, index));
    }
    Map<String, ?> map = (Map<String, ?>) obj;
    Assertions.assertTrue(map.containsKey(path.get(index)), "Loaded map does not contain path " + path.subList(0, index + 1));
    if (index < path.size() - 1) {
      assertMessageConfigLinesContainsKey(path, index + 1, map.get(path.get(index)));
    }
  }

  @Test
  void messageConfigFileComplete() throws IOException, URISyntaxException {
    List<String> englishKeys = getEnglishKeys();
    URL url = getClass().getClassLoader().getResource("messages.yml");
    Assertions.assertNotNull(url, "Could not get resource for messages.yml");
    String uncommentedConfig = Files.readAllLines(Path.of(url.toURI()), StandardCharsets.UTF_8)
        .stream()
        .map(s -> s.substring(2))
        .collect(Collectors.joining("\n"));
    Object loaded = new Yaml().load(uncommentedConfig);
    for (String expectedKey : englishKeys) {
      assertMessageConfigLinesContainsKey(Arrays.stream(expectedKey.split("\\.")).toList(), 0, loaded);
    }
  }

}
