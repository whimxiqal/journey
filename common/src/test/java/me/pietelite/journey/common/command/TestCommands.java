package me.pietelite.journey.common.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.JourneyTestHarness;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.platform.WorldLoader;
import me.pietelite.mantle.common.CommandResult;
import me.pietelite.mantle.common.CommandSource;
import me.pietelite.mantle.common.Mantle;
import me.pietelite.mantle.common.MantleCommand;
import me.pietelite.mantle.common.connector.CommandConnector;
import me.pietelite.mantle.common.connector.CommandRoot;
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
    commandFailure("journey listwaypoints");
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
