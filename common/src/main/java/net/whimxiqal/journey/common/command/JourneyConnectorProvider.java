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

package net.whimxiqal.journey.common.command;

import java.util.Collections;
import java.util.stream.Collectors;
import net.whimxiqal.journey.common.Journey;
import net.whimxiqal.journey.common.JourneyLexer;
import net.whimxiqal.journey.common.JourneyParser;
import net.whimxiqal.journey.common.integration.Scope;
import net.whimxiqal.journey.common.message.Formatter;
import net.whimxiqal.mantle.common.CommandSource;
import net.whimxiqal.mantle.common.connector.CommandConnector;
import net.whimxiqal.mantle.common.connector.CommandRoot;
import net.kyori.adventure.text.Component;
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
        .playerOnlyCommands(
            JourneyParser.RULE_waypoint,
            JourneyParser.RULE_serverSetWaypoint)
        .identifierInfo(IdentifierInfo.builder(JourneyParser.RULE_identifier, JourneyParser.IdentifierContext.class)
                        .extractor(ctx -> ctx.ident().stream().map(ParseTree::getText).collect(Collectors.joining(" ")))
                    .addIgnoredCompletionToken(JourneyLexer.SINGLE_QUOTE)
                    .addIgnoredCompletionToken(JourneyLexer.DOUBLE_QUOTE)
                    .addParameter(Parameter.builder("waypoint")
                        .options(ctx -> {
                        if (ctx.source().type() == CommandSource.Type.PLAYER) {
                           return Journey.get().dataManager().personalWaypointManager().getAll(ctx.source().uuid()).keySet();
                          }
                        return Collections.emptyList();                })
                        .build())
                .addParameter(Parameter.builder("scope")
                        .options(ctx -> Scope.root(ctx.source()).options())
                        .build())
                .registerCompletion(JourneyParser.RULE_waypoint, 0, "waypoint")
                    .registerCompletion(JourneyParser.RULE_journeyto, 0, "scope")             .registerCompletion(JourneyParser.RULE_player, 0, "player")
                    .build())

        .lexer(JourneyLexer.class)
        .parser(JourneyParser.class)
        .executor(new JourneyExecutor())
        .playerOnlyCommands(
            JourneyParser.RULE_journeyto,
            JourneyParser.RULE_waypoint,
            JourneyParser.RULE_setwaypoint,
            JourneyParser.RULE_serverSetWaypoint)
        .build();
  }

}
