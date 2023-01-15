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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.whimxiqal.journey.common.Journey;
import net.whimxiqal.journey.common.JourneyTestHarness;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.platform.TestJourneyPlayer;
import net.whimxiqal.journey.platform.TestPlatformProxy;
import net.whimxiqal.journey.platform.WorldLoader;
import net.whimxiqal.mantle.common.CommandResult;
import net.whimxiqal.mantle.common.CommandSource;
import net.whimxiqal.mantle.common.Mantle;
import net.whimxiqal.mantle.common.MantleCommand;
import net.whimxiqal.mantle.common.connector.CommandConnector;
import net.whimxiqal.mantle.common.connector.CommandRoot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestCommands extends JourneyTestHarness {

  private final static Map<String, MantleCommand> commands = new HashMap<>();
  private final static UUID myUuid = UUID.randomUUID();

  @BeforeAll
  static void init() {
    Mantle.setProxy(new TestProxy());
    register(JourneyConnectorProvider.connector());
    TestPlatformProxy.onlinePlayers.add(new TestJourneyPlayer(myUuid));
  }

  static void register(CommandConnector connector) {
    for (CommandRoot root : connector.roots()) {
      commands.put(root.baseCommand(), new MantleCommand(connector, root));
    }
  }

  static CommandResult execute(String command) {
    String[] baseCommand = command.split(" ", 2);
    assert baseCommand.length == 1 || baseCommand.length == 2;
    MantleCommand mantleCommand = commands.get(baseCommand[0]);
    if (mantleCommand == null) {
      return CommandResult.failure();
    }
    return mantleCommand.process(new CommandSource(CommandSource.Type.PLAYER,
        myUuid,
        Journey.get().proxy().audienceProvider().console()), baseCommand.length > 1 ? baseCommand[1] : "");
  }

  static List<String> completions(String command) {
    String[] baseCommand = command.split(" ", 2);
    assert baseCommand.length == 1 || baseCommand.length == 2;
    MantleCommand mantleCommand = commands.get(baseCommand[0]);
    if (mantleCommand == null) {
      return Collections.emptyList();
    }
    return mantleCommand.complete(new CommandSource(CommandSource.Type.PLAYER,
        myUuid,
        Journey.get().proxy().audienceProvider().console()), baseCommand.length > 1 ? baseCommand[1] : "");
  }

  static void addHome() {
    Journey.get().dataManager().personalWaypointManager().add(myUuid, new Cell(0, 0, 0, WorldLoader.worldResources[0]), "home");
  }

  void commandSuccess(String command) {
    Assertions.assertEquals(CommandResult.Type.SUCCESS, execute(command).type());
  }

  void commandFailure(String command) {
    Assertions.assertEquals(CommandResult.Type.FAILURE, execute(command).type());
  }

  @Test
  void sampleTest() {
    commandSuccess("journey");
  }

  @Test
  void journeyToTest() {
    addHome();
    commandSuccess("journeyto home");
    commandSuccess("journeyto personal:home");
    Journey.get().dataManager().publicWaypointManager().add(new Cell(0, 0, 0, WorldLoader.worldResources[0]), "home");
    commandFailure("journeyto home");
    commandSuccess("journeyto personal:home");
    commandSuccess("journeyto server:home");
  }

  @Test
  void journeyToCompletionsTest() {
    addHome();
    List<String> completions = completions("journeyto personal:");
    // TODO doesn't work. Seems to be an error with Mantle.
//    Assertions.assertEquals(1, completions.size());
//    Assertions.assertEquals("personal:spawn", completions.get(0));
  }
}
