package net.whimxiqal.journey.message;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import net.whimxiqal.journey.config.LocaleSetting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

}
