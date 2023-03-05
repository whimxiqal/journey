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

package net.whimxiqal.journey.command;

import java.util.Collections;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.common.JourneyLexer;
import net.whimxiqal.journey.common.JourneyParser;
import net.whimxiqal.journey.JourneyPlayerImpl;
import net.whimxiqal.journey.scope.ScopeUtil;
import net.whimxiqal.journey.util.Permission;
import net.whimxiqal.mantle.common.CommandSource;
import net.whimxiqal.mantle.common.connector.CommandConnector;
import net.whimxiqal.mantle.common.connector.CommandRoot;
import net.whimxiqal.mantle.common.connector.IdentifierInfo;
import net.whimxiqal.mantle.common.parameter.Parameter;
import org.antlr.v4.runtime.tree.ParseTree;

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
        .identifierInfo(IdentifierInfo.builder(JourneyParser.RULE_identifier, JourneyParser.IdentifierContext.class)
            .extractor(ctx -> ctx.ident().stream().map(ParseTree::getText).collect(Collectors.joining(" ")))
            .addIgnoredCompletionToken(JourneyLexer.SINGLE_QUOTE)
            .addIgnoredCompletionToken(JourneyLexer.DOUBLE_QUOTE)
            .addParameter(Parameter.builder("waypoint")
                .options(ctx -> {
                  if (ctx.source().type() == CommandSource.Type.PLAYER) {
                    return Journey.get().dataManager().personalWaypointManager().getAll(ctx.source().uuid(), false).keySet();
                  }
                  return Collections.emptyList();
                })
                .build())
            .addParameter(Parameter.builder("server-waypoint")
                .options(ctx -> Journey.get().dataManager().publicWaypointManager().getAll().keySet())
                .build())
            .addParameter(Parameter.builder("scope")
                .options(ctx -> ScopeUtil.options(JourneyPlayerImpl.from(ctx.source())))
                .build())
            .registerCompletion(JourneyParser.RULE_waypoint, 0, "waypoint")
            .registerCompletion(JourneyParser.RULE_serverWaypoint, 0, "server-waypoint")
            .registerCompletion(JourneyParser.RULE_journeytoTarget, 0, "scope")
            .registerCompletion(JourneyParser.RULE_player, 0, "player")
            .build())
        // RULE_waypoint is handled in executor
        .addPermission(JourneyParser.RULE_setwaypoint, Permission.EDIT_PERSONAL.path())
        .addPermission(JourneyParser.RULE_listwaypoints, Permission.PATH_PERSONAL.path())
        .addPermission(JourneyParser.RULE_unsetWaypoint, Permission.EDIT_PERSONAL.path())
        .addPermission(JourneyParser.RULE_renameWaypoint, Permission.EDIT_PERSONAL.path())
        .addPermission(JourneyParser.RULE_publicWaypoint, Permission.EDIT_PERSONAL_PUBLICITY.path())
        // RULE_player is handled in executor
        .addPermission(JourneyParser.RULE_playerWaypoint, Permission.PATH_PLAYER_WAYPOINTS.path())
        .addPermission(JourneyParser.RULE_serverSetWaypoint, Permission.EDIT_SERVER.path())
        .addPermission(JourneyParser.RULE_serverListWaypoints, Permission.PATH_SERVER.path())
        // RULE_serverWaypoint is handled in executor
        .addPermission(JourneyParser.RULE_serverUnsetWaypoint, Permission.EDIT_SERVER.path())
        .addPermission(JourneyParser.RULE_serverRenameWaypoint, Permission.EDIT_SERVER.path())
        .addPermission(JourneyParser.RULE_debug, Permission.ADMIN_DEBUG.path())
        .addPermission(JourneyParser.RULE_cache, Permission.ADMIN_CACHE.path())
        .addPermission(JourneyParser.RULE_listPortals, Permission.ADMIN_INFO.path())
        // other admin commands are handled in executor
        .addPermission(JourneyParser.RULE_cancel, Permission.CANCEL.path())
        .addPermission(JourneyParser.RULE_timeoutFlag, Permission.FLAG_TIMEOUT.path())
        .addPermission(JourneyParser.RULE_animateFlag, Permission.FLAG_ANIMATE.path())
        .addPermission(JourneyParser.RULE_flyFlag, Permission.FLAG_FLY.path())
        .addPermission(JourneyParser.RULE_doorFlag, Permission.FLAG_DOOR.path())
        .addPermission(JourneyParser.RULE_digFlag, Permission.FLAG_DIG.path())
        .lexer(JourneyLexer.class)
        .parser(JourneyParser.class)
        .executor(new JourneyExecutor())
        .playerOnlyCommands(
            JourneyParser.RULE_journeyto,
            JourneyParser.RULE_setwaypoint,
            JourneyParser.RULE_listwaypoints,
            JourneyParser.RULE_waypoint,
            JourneyParser.RULE_player,
            JourneyParser.RULE_serverSetWaypoint)
        .build();
  }

}
