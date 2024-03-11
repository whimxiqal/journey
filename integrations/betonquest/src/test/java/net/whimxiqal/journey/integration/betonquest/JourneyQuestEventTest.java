package net.whimxiqal.journey.integration.betonquest;

import java.lang.reflect.Field;
import java.util.logging.Logger;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.modules.logger.DefaultBetonQuestLogger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class JourneyQuestEventTest {

  public void runTestCreateQuestEvent(String params, boolean succeed) throws InstructionParseException, NoSuchFieldException, IllegalAccessException {
    Plugin plugin = Mockito.mock(Plugin.class);
    Logger logger = Logger.getLogger("testCreateQuestEvent");
    BetonQuestLogger betonQuestLogger = new DefaultBetonQuestLogger(plugin, logger, JourneyQuestEventTest.class, "test");

    BetonQuest betonQuest = Mockito.mock(BetonQuest.class);
    Field instanceField = BetonQuest.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, betonQuest);

    BetonQuestLoggerFactory loggerFactory = Mockito.mock(BetonQuestLoggerFactory.class);
    Mockito.when(betonQuest.getLoggerFactory()).thenReturn(loggerFactory);

    Mockito.when(loggerFactory.create(Mockito.any(Class.class))).thenReturn(betonQuestLogger);

    // Mock world creation so Instruction can create a world
    Server server = Mockito.mock(Server.class);
    Field serverField = Bukkit.class.getDeclaredField("server");
    serverField.setAccessible(true);
    serverField.set(null, server);
    Mockito.when(server.getWorld(Mockito.any(String.class))).thenReturn(Mockito.mock(World.class));

    QuestPackage questPackage = Mockito.mock(QuestPackage.class);
    Instruction instruction = new Instruction(betonQuestLogger, questPackage, null, params);
    if (succeed) {
      new JourneyQuestEvent(instruction);
    } else {
      Assertions.assertThrows(InstructionParseException.class, () -> new JourneyQuestEvent(instruction));
    }
  }

  @Test
  public void testCreateQuestEvent() throws InstructionParseException, NoSuchFieldException, IllegalAccessException {
    runTestCreateQuestEvent("journey notALocation", false);
    runTestCreateQuestEvent("journey 1;1;1;world", true);  // no flags

    // Timeout flag
    runTestCreateQuestEvent("journey 1;1;1;world timeout=asdf", false);  // not numbers
    runTestCreateQuestEvent("journey 1;1;1;world timeout=(100", false);  // wrong format
    runTestCreateQuestEvent("journey 1;1;1;world timeout=100)", false);  // wrong format
    runTestCreateQuestEvent("journey 1;1;1;world timeout=100", true);
    runTestCreateQuestEvent("journey 1;1;1;world timeout=(100)", true);

    // Fly flag
    runTestCreateQuestEvent("journey 1;1;1;world fly=asdf", false);  // not boolean
    runTestCreateQuestEvent("journey 1;1;1;world fly=true", true);
    runTestCreateQuestEvent("journey 1;1;1;world fly=false", true);
    runTestCreateQuestEvent("journey 1;1;1;world fly=(true)", true);
    runTestCreateQuestEvent("journey 1;1;1;world fly=(false)", true);

    // Messages
    runTestCreateQuestEvent("journey 1;1;1;world successMessage=Success!", true);
    runTestCreateQuestEvent("journey 1;1;1;world successMessage=Success! Wahoo!", false);  // missing parens
    runTestCreateQuestEvent("journey 1;1;1;world successMessage=(Success! Wahoo!)", true);
    runTestCreateQuestEvent("journey 1;1;1;world successMessage=( space first )", true);

    // Navigator
    runTestCreateQuestEvent("journey 1;1;1;world navigator=asdf", false);  // wrong format
    runTestCreateQuestEvent("journey 1;1;1;world navigator:type=asdf", true);
    runTestCreateQuestEvent("journey 1;1;1;world navigator:foo=bar", true);
    runTestCreateQuestEvent("journey 1;1;1;world navigator:foo=bar bar", false);  // missing parens
    runTestCreateQuestEvent("journey 1;1;1;world navigator:foo=(bar bar)", true);

    // Multiple
    runTestCreateQuestEvent("journey 1;1;1;world timeout=60 fly=false successMessage=(Success! Great work. Who knew?) failureMessage=(You thought? RIP) navigator:type=npc navigator:entity-type=sheep", true);
  }
}