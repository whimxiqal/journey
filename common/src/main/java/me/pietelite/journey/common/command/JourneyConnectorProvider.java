package me.pietelite.journey.common.command;

import me.pietelite.journey.common.JourneyLexer;
import me.pietelite.journey.common.JourneyParser;
import me.pietelite.mantle.common.connector.CommandConnector;
import me.pietelite.mantle.common.connector.CommandRoot;
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
              .description(Component.text("JourneySession to destinations"))
              .build())
          .playerOnlyCommands(JourneyParser.RULE_waypoint,
              JourneyParser.RULE_serverSetWaypoint)
          .lexer(JourneyLexer.class)
          .parser(JourneyParser.class)
          .executor(new JourneyExecutor())
          .build();
  }

}
