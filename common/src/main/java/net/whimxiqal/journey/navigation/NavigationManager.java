package net.whimxiqal.journey.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.message.Messages;
import net.whimxiqal.journey.navigation.option.NavigatorOption;
import net.whimxiqal.journey.navigation.option.NavigatorOptionParser;
import net.whimxiqal.journey.navigation.option.ParseNavigatorOptionException;
import net.whimxiqal.journey.search.SearchStep;
import net.whimxiqal.journey.util.Permission;
import net.whimxiqal.mantle.common.CommandSource;

public class NavigationManager {

  private static final MiniMessage miniMessage = MiniMessage.miniMessage();
  private static final NavigatorOptionParser<Component> componentDeserializer = miniMessage::deserialize;
  public static final String NAVIGATOR_OPTION_ID_COMPLETION_MESSAGE = "completion-message";
  public static final NavigatorOption<Component> COMPLETION_MESSAGE_OPTION = NavigatorOption.builder(NAVIGATOR_OPTION_ID_COMPLETION_MESSAGE, Component.class)
      .defaultValue(Settings.DEFAULT_NAVIGATION_COMPLETION_MESSAGE::getValue)
      .parser(componentDeserializer)
      .build();
  public static final String NAVIGATOR_OPTION_ID_COMPLETION_TITLE = "completion-title";
  public static final NavigatorOption<Component> COMPLETION_TITLE_OPTION = NavigatorOption.builder(NAVIGATOR_OPTION_ID_COMPLETION_TITLE, Component.class)
      .defaultValue(Settings.DEFAULT_NAVIGATION_COMPLETION_TITLE::getValue)
      .parser(componentDeserializer)
      .build();
  public static final String NAVIGATOR_OPTION_ID_COMPLETION_SUBTITLE = "completion-subtitle";
  public static final NavigatorOption<Component> COMPLETION_SUBTITLE_OPTION = NavigatorOption.builder(NAVIGATOR_OPTION_ID_COMPLETION_SUBTITLE, Component.class)
      .defaultValue(Settings.DEFAULT_NAVIGATION_COMPLETION_SUBTITLE::getValue)
      .parser(componentDeserializer)
      .build();
  private final Pattern NAVIGATOR_OPTIONS_PATTERN = Pattern.compile("^([^:,]*:[^:,]*,)*([^:,]*:[^:,]*)?$");
  private final Pattern NAVIGATOR_DEFINITION_PARTIAL_OPTION = Pattern.compile("^((?:[^:,]*:[^:,]*,)*)([^:,]*)$");
  private final Pattern NAVIGATOR_DEFINITION_PARTIAL_VALUE = Pattern.compile("^((?:[^:,]*:[^:,]*,)*([^:,]*):)([^:,]*)$");
  private final Map<String, NavigatorFactory> navigatorFactories = new HashMap<>();
  private final Map<UUID, List<ActiveNavigation>> activeNavigations = new HashMap<>();
  private UUID navigatorUpdateTaskId;

  public NavigationManager() {
    // Register the Trail Navigator
    registerNavigatorFactory(NavigatorFactory.builder(Journey.NAME, TrailNavigator.TRAIL_NAVIGATOR_ID)
        .supplier(TrailNavigator::new)
        .option(TrailNavigator.OPTION_PARTICLE)
        .option(TrailNavigator.OPTION_COLOR)
        .option(TrailNavigator.OPTION_WIDTH)
        .option(TrailNavigator.OPTION_DENSITY)
        .permission(Permission.FLAG_NAVIGATOR_TRAIL.path())
        .build());
  }

  public void registerNavigatorFactory(NavigatorFactory navigatorFactory) {
    String id = navigatorFactory.navigatorType();
    if (navigatorFactories.containsKey(id.toLowerCase(Locale.ENGLISH))) {
      throw new IllegalArgumentException("A navigator supplier with id " + id + " already exists!");
    }
    navigatorFactories.put(id.toLowerCase(Locale.ENGLISH), navigatorFactory);
  }

  public CompletableFuture<NavigationResult> startNavigating(JourneyAgent agent, List<? extends SearchStep> path, NavigatorDetails details) throws IllegalArgumentException {
    Journey.logger().info("startNavigating: " + details);
    NavigatorFactory factory = navigatorFactories.get(details.navigatorType());
    if (factory == null) {
      throw new IllegalArgumentException("Unknown navigator type: " + details.navigatorType());
    }
    Journey.logger().info("startNavigating: factory: " + factory.navigatorType());
    NavigatorOptionValuesImpl optionValues = new NavigatorOptionValuesImpl(factory.options(), details.options());
    NavigationSession session = new NavigationSession(agent, path, optionValues);
    Navigator navigator = factory.navigator(agent, session, optionValues);
    boolean success = navigator.start();
    if (success) {
      activeNavigations.computeIfAbsent(agent.uuid(), k -> new LinkedList<>()).add(new ActiveNavigation(navigator, session));
    } else {
      session.resultFuture().complete(NavigationResult.FAILED_START);
    }
    return session.resultFuture();
  }

  public int stopNavigators(UUID agentUuid) {
    List<ActiveNavigation> navigations = activeNavigations.get(agentUuid);
    if (navigations == null) {
      return 0;
    }
    int navigatorsStopped = navigations.size();
    for (ActiveNavigation navigation : navigations) {
      navigation.navigator.stop();
    }
    activeNavigations.remove(agentUuid);
    return navigatorsStopped;
  }

  public void shutdown() {
    Journey.logger().debug("[Navigator Manager] Shutting down...");
    // stop all navigators
    for (List<ActiveNavigation> navigations : activeNavigations.values()) {
      for (ActiveNavigation navigation : navigations) {
        navigation.navigator.stop();
      }
    }
    activeNavigations.clear();
    if (navigatorUpdateTaskId != null) {
      Journey.get().proxy().schedulingManager().cancelTask(navigatorUpdateTaskId);
    }
  }

  public NavigatorDetails parseNavigatorFlagDefinition(CommandSource src, String navigatorType, String options) throws IllegalArgumentException {
    NavigatorFactory factory = navigatorFactories.get(navigatorType);
    if (factory == null) {
      Messages.COMMAND_NAVIGATION_NO_NAVIGATOR.sendTo(src.audience(), Formatter.ERROR, navigatorType);
      return null;
    }
    Optional<String> permission = factory.permission();
    if (permission.isPresent() && !src.hasPermission(permission.get())) {
      Messages.COMMAND_NAVIGATION_NO_NAVIGATOR_PERMISSION.sendTo(src.audience(), Formatter.ERROR, navigatorType);
      return null;
    }
    Matcher matcher = NAVIGATOR_OPTIONS_PATTERN.matcher(options);
    if (!matcher.matches()) {
      Messages.COMMAND_NAVIGATION_INVALID_OPTIONS.sendTo(src.audience(), Formatter.ERROR, options, navigatorType);
      return null;
    }
    Map<String, NavigatorOption<?>> optionInfos = factory.options();
    Map<String, Object> parsedOptions = new HashMap<>();
    String[] allOptions = matcher.group(2).split(",");
    for (String keyValue : allOptions) {
      String[] splitKeyValue = keyValue.split(":");
      if (splitKeyValue.length != 2) {
        Messages.COMMAND_NAVIGATION_INVALID_OPTION.sendTo(src.audience(), Formatter.ERROR, keyValue);
        return null;
      }
      String optionKey = splitKeyValue[0].toLowerCase(Locale.ENGLISH);
      NavigatorOption<?> optionInfo = optionInfos.get(optionKey);
      if (optionInfo == null) {
        Messages.COMMAND_NAVIGATION_UNKNOWN_OPTION.sendTo(src.audience(), Formatter.ERROR, navigatorType, optionKey);
        return null;
      }
      permission = optionInfo.permission();
      if (permission.isPresent() && !src.hasPermission(permission.get())) {
        Messages.COMMAND_NAVIGATION_NO_OPTION_PERMISSION.sendTo(src.audience(), Formatter.ERROR, optionKey, navigatorType);
        return null;
      }
      if (!parseAndValidateValue(src, optionInfo, optionKey, splitKeyValue[1], parsedOptions)) {
        return null;
      }
    }
    return new NavigatorDetails(navigatorType, parsedOptions);
  }

  private <X> boolean parseAndValidateValue(CommandSource src, NavigatorOption<X> optionInfo,
                                            String optionId, String optionValue,
                                            Map<String, Object> parsedOptions) {
    Optional<NavigatorOptionParser<X>> parser = optionInfo.parser();
    if (parser.isEmpty()) {
      Messages.COMMAND_NAVIGATION_NO_INGAME_OPTION.sendTo(src.audience(), Formatter.ERROR, optionId);
      return false;
    }
    X parsed;
    try {
      parsed = parser.get().parse(optionValue);
    } catch (ParseNavigatorOptionException e) {
      Messages.COMMAND_NAVIGATION_OPTION_VALUE_ERROR.sendTo(src.audience(), Formatter.ERROR, optionValue, optionId);
      return false;
    }
    String validationError = optionInfo.validate(parsed);
    if (validationError == null) {
      parsedOptions.put(optionId, parsed);
    } else {
      Messages.COMMAND_NAVIGATION_OPTION_VALUE_INVALID.sendTo(src.audience(), Formatter.ERROR, optionValue, optionId);
      return false;
    }
    Optional<String> valuePermission = optionInfo.valuePermission(parsed);
    if (valuePermission.isPresent() && !src.hasPermission(valuePermission.get())) {
      Messages.COMMAND_NAVIGATION_NO_OPTION_VALUE_PERMISSION.sendTo(src.audience(), Formatter.ERROR, optionValue, optionId);
      return false;
    }
    return true;
  }

  public List<String> provideNavigatorOptionsSuggestions(CommandSource src, String navigatorType, String partialOptions) {
    List<String> suggestions = new LinkedList<>();
    NavigatorFactory factory = navigatorFactories.get(navigatorType);
    if (factory == null) {
      return suggestions;  // no suggestions
    }

    // Check if source is typing a navigator option
    Matcher matcher = NAVIGATOR_DEFINITION_PARTIAL_OPTION.matcher(partialOptions);
    if (matcher.matches()) {
      String prefix = matcher.group(1);
      String partialOption = matcher.group(3);
      for (Map.Entry<String, NavigatorOption<?>> entry : factory.options().entrySet()) {
        if (entry.getValue().permission().map(perm -> !src.hasPermission(perm)).orElse(false)) {
          continue;
        }
        if (entry.getValue().parser().isEmpty()) {
          continue;
        }
        if (entry.getKey().toLowerCase(Locale.ENGLISH).startsWith(partialOption)) {
          suggestions.add(prefix + entry.getKey() + ":");
        }
      }
      return suggestions;
    }

    // Check if source is typing a navigator option value
    matcher = NAVIGATOR_DEFINITION_PARTIAL_VALUE.matcher(partialOptions);
    if (matcher.matches()) {
      String option = matcher.group(3);
      NavigatorOption<?> optionInfo = factory.options().get(option);
      if (optionInfo == null) {
        return suggestions;  // no suggestions
      }
      String prefix = matcher.group(1);
      String partialValue = matcher.group(4);
      for (String optionSuggestion : optionInfo.valueSuggestions()) {
        String suggestionLower = optionSuggestion.toLowerCase(Locale.ENGLISH);
        if (suggestionLower.equals(partialValue)) {
          suggestions.add(prefix + optionSuggestion + ",");
          suggestions.add(prefix + optionSuggestion + "}");
        } else if (optionSuggestion.toLowerCase(Locale.ENGLISH).startsWith(partialValue)) {
          suggestions.add(prefix + optionSuggestion);
        }
      }
      return suggestions;
    }

    // no matches, no suggestions
    return suggestions;
  }

  public void updateLocation(UUID playerUuid, Cell location) {
    List<ActiveNavigation> navigations = activeNavigations.get(playerUuid);
    if (navigations == null) {
      return;
    }
    Iterator<ActiveNavigation> navigationIterator = navigations.iterator();
    while (navigationIterator.hasNext()) {
      ActiveNavigation navigation = navigationIterator.next();
      boolean isDone = navigation.session.visit(location);
      if (isDone) {
        navigation.navigator.stop();
        navigationIterator.remove();
      }
    }
  }

  public List<UUID> navigatingAgents() {
    return new ArrayList<>(activeNavigations.keySet());
  }

  public List<String> navigators() {
    return new ArrayList<>(navigatorFactories.keySet());
  }

  public void stopNavigatorsDependentOn(String pluginName) {
    for (Iterator<Map.Entry<UUID, List<ActiveNavigation>>> agentsIt = activeNavigations.entrySet().iterator(); agentsIt.hasNext(); ) {
      Map.Entry<UUID, List<ActiveNavigation>> agentEntry = agentsIt.next();
      for (Iterator<ActiveNavigation> navigationIt = agentEntry.getValue().iterator(); navigationIt.hasNext(); ) {
        ActiveNavigation navigation = navigationIt.next();
        if (navigation.navigator.pluginDependencies().contains(pluginName)) {
          navigation.navigator.stop();
          navigation.session.resultFuture().complete(NavigationResult.FAILED_SHUTDOWN);
          navigationIt.remove();
        }
      }
      if (agentEntry.getValue().isEmpty()) {
        agentsIt.remove();
      }
    }
  }

  public void initialize() {
    navigatorUpdateTaskId = Journey.get().proxy().schedulingManager().scheduleRepeat(() -> {
      for (Iterator<Map.Entry<UUID, List<ActiveNavigation>>> agentsIt = activeNavigations.entrySet().iterator(); agentsIt.hasNext(); ) {
        Map.Entry<UUID, List<ActiveNavigation>> agentEntry = agentsIt.next();
        for (Iterator<ActiveNavigation> navigationIt = agentEntry.getValue().iterator(); navigationIt.hasNext(); ) {
          ActiveNavigation navigation = navigationIt.next();
          if (navigation.navigator.shouldStop()) {
            navigation.navigator.stop();
            navigation.session.resultFuture().complete(NavigationResult.FAILED_RUNNING);
            navigationIt.remove();
          } else if (navigation.session.resultFuture().isCancelled()) {
            navigation.navigator.stop();
            navigationIt.remove();
          }
        }
        if (agentEntry.getValue().isEmpty()) {
          agentsIt.remove();
        }
      }
    }, false, 1);
  }

  private record ActiveNavigation(Navigator navigator, NavigationSession session) {
  }

}
