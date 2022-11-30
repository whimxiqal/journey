/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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
            .addParameter("scoped", src -> Scope.root(src).options())
            .registerCompletion(JourneyParser.RULE_waypoint, JourneyParser.RULE_identifier, 0, "waypoint")
            .registerCompletion(JourneyParser.RULE_journeyto, JourneyParser.RULE_journeytoTarget, 0, "scoped")
            .registerCompletion(JourneyParser.RULE_player, JourneyParser.RULE_identifier, 0, "player")
            .build())
        .lexer(JourneyLexer.class)
        .parser(JourneyParser.class)
        .executor(new JourneyExecutor())
        .helpInfo(HelpCommandInfo.builder()
            .setHeader(Formatter.prefix())
            .addDescription(JourneyParser.RULE_journey, Formatter.dull("Base command for the Journey plugin"))
            .addDescription(JourneyParser.RULE_journeyto, Formatter.dull("Search for a path using shorthand"))
            .addDescription(JourneyParser.RULE_setwaypoint, Formatter.dull("Set a waypoint where you are standing"))
            .addDescription(JourneyParser.RULE_listwaypoints, Formatter.dull("List all of your waypoints"))
            .addDescription(JourneyParser.RULE_waypoint, Formatter.dull("Use your waypoints"))
            .addDescription(JourneyParser.RULE_unsetWaypoint, Formatter.dull("Unset one of your waypoints"))
            .addDescription(JourneyParser.RULE_renameWaypoint, Formatter.dull("Rename one of your waypoints"))
            .addDescription(JourneyParser.RULE_publicWaypoint, Formatter.dull("Make one of your waypoints public or private"))
            .addDescription(JourneyParser.RULE_player, Formatter.dull("Search for a player or one of their waypoints"))
            .addDescription(JourneyParser.RULE_playerWaypoint, Formatter.dull("Search for a player's public waypoint"))
            .addDescription(JourneyParser.RULE_server, Formatter.dull("Search for a server-wide waypoint"))
            .addDescription(JourneyParser.RULE_serverSetWaypoint, Formatter.dull("Set a server-wide waypoint"))
            .addDescription(JourneyParser.RULE_serverListWaypoints, Formatter.dull("List all server-wide waypoints"))
            .addDescription(JourneyParser.RULE_serverWaypoint, Formatter.dull("Use the server-wide waypoints"))
            .addDescription(JourneyParser.RULE_unsetServerWaypoint, Formatter.dull("Unset a server-wide waypoint"))
            .addDescription(JourneyParser.RULE_renameServerWaypoint, Formatter.dull("Rename a server-wide waypoint"))
            .addDescription(JourneyParser.RULE_admin, Formatter.dull("Root of admin commands"))
            .addDescription(JourneyParser.RULE_debug, Formatter.dull("Turn on debug mode"))
            .addDescription(JourneyParser.RULE_surface, Formatter.dull("Search for a path to the surface"))
            .addDescription(JourneyParser.RULE_death, Formatter.dull("Search for a path to your last known death location"))
            .addDescription(JourneyParser.RULE_cancel, Formatter.dull("Cancel an ongoing search or path"))
            .addIgnored(JourneyParser.RULE_identifier)
            .addIgnored(JourneyParser.RULE_ident)
            .addIgnored(JourneyParser.RULE_flagSet)  // other flags are ignored because flagSet is ignored
            .build())
        .build();
  }

}
