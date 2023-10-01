package net.whimxiqal.journey.config.struct;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.whimxiqal.journey.config.serializer.TypeDeserializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public record ConfigItemsRule(Predicate<String> ruleMatcher, List<ConfigItemType> items) {

  public ConfigItemsRule(String regex, List<ConfigItemType> items) {
    this(Pattern.compile(regex).asMatchPredicate(), items);
  }

  public static class Deserializer extends TypeDeserializer<ConfigItemsRule> {

    @Override
    public ConfigItemsRule deserialize(Type type, ConfigurationNode node) throws SerializationException {
      String ruleRegex = node.node("rule").getString();
      if (ruleRegex == null) {
        throw new SerializationException("Invalid rule list, rule regex is null");
      }
      List<ConfigItemType> items = node.node("items").getList(ConfigItemType.class);
      if (items == null || items.isEmpty()) {
        throw new SerializationException("Invalid rule list, there are no items");
      }
      Pattern rule;
      try {
        rule = Pattern.compile(ruleRegex);
      } catch (PatternSyntaxException e) {
        throw new SerializationException(e);
      }
      return new ConfigItemsRule(rule.asMatchPredicate(), items);
    }
  }

}
