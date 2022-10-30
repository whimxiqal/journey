package me.pietelite.journey.common.command;

import java.util.Collections;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.JourneyLexer;
import me.pietelite.journey.common.JourneyParser;
import me.pietelite.journey.common.integration.Scope;
import me.pietelite.journey.common.message.Formatter;
import me.pietelite.mantle.common.CommandSource;
import me.pietelite.mantle.common.connector.CommandConnector;
import me.pietelite.mantle.common.connector.CommandRoot;
import me.pietelite.mantle.common.connector.CompletionInfo;
import me.pietelite.mantle.common.connector.HelpCommandInfo;
import net.kyori.adventure.text.Component;

public class JourneyConnectorProvider {

  public static CommandConnector connector() {
    return CommandConnector.builder()
        .addRoot(CommandRoot.builder("journey")
            .addAlias("jo")
            .description(Component.text("All journey commands"))
            .build())
        .addRoot(CommandRoot.builder("journeyto")
            .addAlias("jt")
            .description(Component.text("Journey to destinations"))
            .build())
        .playerOnlyCommands(JourneyParser.RULE_waypoint,
            JourneyParser.RULE_serverSetWaypoint)
        .completionInfo(CompletionInfo.builder()
            .addIgnoredCompletionToken(JourneyLexer.SINGLE_QUOTE)
            .addIgnoredCompletionToken(JourneyLexer.DOUBLE_QUOTE)
            .addParameter("waypoint", src -> {
              if (src.type() == CommandSource.Type.PLAYER) {
                return Journey.get().dataManager().personalWaypointManager().getAll(src.uuid()).keySet();
              }
              return Collections.emptyList();
            })
            .addParameter("scope", src -> Scope.root(src).options())
            .registerCompletion(JourneyParser.RULE_waypoint, JourneyParser.RULE_identifier, 0, "waypoint")
            .registerCompletion(JourneyParser.RULE_journeyto, JourneyParser.RULE_journeytoTarget, 0, "scope")
            .registerCompletion(JourneyParser.RULE_player, JourneyParser.RULE_identifier, 0, "player")
            .build())
        .lexer(JourneyLexer.class)
        .parser(JourneyParser.class)
        .executor(new JourneyExecutor())
        .helpInfo(HelpCommandInfo.builder().setHeader(Formatter.prefix()).build())
        .build();
  }

}
