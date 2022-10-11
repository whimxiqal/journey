package me.pietelite.journey.common.command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.JourneyBaseVisitor;
import me.pietelite.journey.common.JourneyParser;
import me.pietelite.journey.common.config.Settings;
import me.pietelite.journey.common.data.PersonalWaypointManager;
import me.pietelite.journey.common.manager.DebugManager;
import me.pietelite.journey.common.message.Formatter;
import me.pietelite.journey.common.message.Pager;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.search.PlayerDestinationGoalSearchSession;
import me.pietelite.journey.common.search.flag.FlagSet;
import me.pietelite.journey.common.search.flag.Flags;
import me.pietelite.journey.common.util.Validator;
import me.pietelite.mantle.common.CommandExecutor;
import me.pietelite.mantle.common.CommandResult;
import me.pietelite.mantle.common.CommandSource;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public class JourneyExecutor implements CommandExecutor {

  @Override
  public ParseTreeVisitor<CommandResult> provide(CommandSource src) {
    return new JourneyBaseVisitor<CommandResult>() {
      final List<String> identifiers = new LinkedList<>();
      final FlagSet flags = new FlagSet();

      @Override
      public CommandResult visitJourney(JourneyParser.JourneyContext ctx) {
        CommandResult result = visitChildren(ctx);
        if (result != null) {
          return result;
        }
        src.audience().sendMessage(Formatter.WELCOME);
        return CommandResult.success();
      }

      @Override
      public CommandResult visitJourneyto(JourneyParser.JourneytoContext ctx) {
        String fullText = ctx.identifier().stream().map(identifier -> identifier.ID().getText()).collect(Collectors.joining(" "));
        src.audience().sendMessage(Formatter.success("Walking to ___", fullText));
        return CommandResult.success();
      }

      @Override
      public CommandResult visitDebug(JourneyParser.DebugContext ctx) {
        DebugManager mgr = Journey.get().debugManager();
        switch (src.type()) {
          case CONSOLE:
            if (mgr.isConsoleDebugging()) {
              mgr.setConsoleDebugging(false);
              src.audience().sendMessage(Formatter.success("Debug mode ___.", "disabled"));
            } else {
              mgr.setConsoleDebugging(true);
              src.audience().sendMessage(Formatter.success("Debug mode ___.", "enabled"));
            }
            return CommandResult.success();
          case PLAYER:
            String targetText = ctx.target.getText();
            if (targetText.isEmpty()) {
              if (mgr.isDebugging(src.uuid())) {
                mgr.stopDebugging(src.uuid());
                src.audience().sendMessage(Formatter.success("Debug mode ___.", "disabled"));
              } else {
                mgr.startDebuggingAll(src.uuid());
                src.audience().sendMessage(Formatter.success("Debug mode ___ on all users.", "enabled"));
              }
            } else {
              Optional<UUID> target = Journey.get().proxy().platform().onlinePlayer(targetText);
              if (!target.isPresent()) {
                src.audience().sendMessage(Formatter.error("Could not find that player"));
                return CommandResult.failure();
              }
              mgr.startDebuggingPlayer(src.uuid(), target.get());
              src.audience().sendMessage(Formatter.success("Debug mode ___ on ___.", "enabled ", targetText));
            }
            return CommandResult.success();
          default:
            return CommandResult.failure();
        }
      }

      @Override
      public CommandResult visitSetwaypoint(JourneyParser.SetwaypointContext ctx) {
        String name = ctx.name.ID().getText();
        if (Validator.isInvalidDataName(name)) {
          src.audience().sendMessage(Formatter.error("That name is invalid"));
          return CommandResult.failure();
        }

        PersonalWaypointManager personalWaypointManager = Journey.get().proxy().dataManager().personalEndpointManager();

        Cell location = Journey.get().proxy().platform().entityLocation(src.uuid());
        String existingName = personalWaypointManager.getName(src.uuid(), location);
        if (existingName != null) {
          src.audience().sendMessage(Formatter.error("Waypoint ___ already exists at this location", existingName));
          return CommandResult.failure();
        }

        if (personalWaypointManager.hasPersonalEndpoint(src.uuid(), name)) {
          src.audience().sendMessage(Formatter.error("A waypoint called ___ already exists", ctx.name.getText()));
          return CommandResult.failure();
        }

        personalWaypointManager.add(src.uuid(), location, name);
        src.audience().sendMessage(Formatter.success("Set waypoint ___", name));
        return CommandResult.success();
      }

      @Override
      public CommandResult visitListwaypoints(JourneyParser.ListwaypointsContext ctx) {
        String pageText = ctx.page.getText();
        int page;
        if (pageText.isEmpty()) {
          page = 1;
        } else {
          try {
            page = Integer.parseUnsignedInt(pageText);
          } catch (NumberFormatException e) {
            src.audience().sendMessage(Formatter.error("Page number is invalid"));
            return CommandResult.failure();
          }
        }

        Map<String, Cell> cells = Journey.get().proxy().dataManager()
            .personalEndpointManager()
            .getAll(src.uuid());

        if (cells.isEmpty()) {
          src.audience().sendMessage(Formatter.warn("You have no saved waypoints yet!"));
          return CommandResult.success();
        }

        List<Map.Entry<String, Cell>> sortedEntryList = new ArrayList<>(cells.entrySet());
        sortedEntryList.sort(Map.Entry.comparingByKey());
        Pager.of(sortedEntryList, Map.Entry::getKey, entry -> entry.getValue().toString())
            .sendPage(src.audience(), page);

        return CommandResult.success();
      }

      @Override
      public CommandResult visitWaypoint(JourneyParser.WaypointContext ctx) {
        String name = ctx.name.ID().getText();
        identifiers.add(name);
        CommandResult childrenResult = visitChildren(ctx);
        if (childrenResult != null) {
          return childrenResult;
        }
        // No other command specified, so journey to the waypoint

        PersonalWaypointManager personalWaypointManager = Journey.get().proxy().dataManager().personalEndpointManager();
        Cell endLocation = personalWaypointManager.getWaypoint(src.uuid(), name);

        if (endLocation == null) {
          src.audience().sendMessage(Formatter.error("Could not find a waypoint called ___", name));
          return CommandResult.failure();
        }

        PlayerDestinationGoalSearchSession session = new PlayerDestinationGoalSearchSession(src.uuid(),
            Journey.get().proxy().platform().entityLocation(src.uuid()),
            endLocation,
            flags);

        Journey.get().searchManager().launchSearch(src.uuid(), session);
        return CommandResult.success();
      }

      @Override
      public CommandResult visitUnsetWaypoint(JourneyParser.UnsetWaypointContext ctx) {
        PersonalWaypointManager endpointManager = Journey.get().proxy().dataManager().personalEndpointManager();
        assert identifiers.size() >= 1;
        String name = identifiers.get(0);
        if (endpointManager.hasPersonalEndpoint(src.uuid(), name)) {
          Journey.get().proxy().dataManager()
              .personalEndpointManager()
              .remove(src.uuid(), name);
          src.audience().sendMessage(Formatter.success("Waypoint ___ has been removed", name));
          return CommandResult.success();
        } else {
          src.audience().sendMessage(Formatter.error("Waypoint ___ could not be found", name));
          return CommandResult.failure();
        }
      }

      @Override
      public CommandResult visitRenameWaypoint(JourneyParser.RenameWaypointContext ctx) {
        return unimplemented();
      }

      @Override
      public CommandResult visitPlayer(JourneyParser.PlayerContext ctx) {
        Optional<UUID> maybePlayer = Journey.get().proxy().platform().onlinePlayer(ctx.PLAYER().getText());
        if (!maybePlayer.isPresent()) {
          src.audience().sendMessage(Formatter.noPlayer(ctx.PLAYER().getText()));
          return CommandResult.failure();
        }
        PlayerDestinationGoalSearchSession session = new PlayerDestinationGoalSearchSession(src.uuid(),
            Journey.get().proxy().platform().entityLocation(src.uuid()),
            Journey.get().proxy().platform().entityLocation(maybePlayer.get()),
            flags);

        Journey.get().searchManager().launchSearch(src.uuid(), session);
        return CommandResult.success();
      }

      @Override
      public CommandResult visitPlayerWaypoint(JourneyParser.PlayerWaypointContext ctx) {
        return super.visitPlayerWaypoint(ctx);
      }

      @Override
      public CommandResult visitServer(JourneyParser.ServerContext ctx) {
        return super.visitServer(ctx);
      }

      @Override
      public CommandResult visitServerSetWaypoint(JourneyParser.ServerSetWaypointContext ctx) {
        return super.visitServerSetWaypoint(ctx);
      }

      @Override
      public CommandResult visitServerListWaypoints(JourneyParser.ServerListWaypointsContext ctx) {
        return super.visitServerListWaypoints(ctx);
      }

      @Override
      public CommandResult visitServerWaypoint(JourneyParser.ServerWaypointContext ctx) {
        return super.visitServerWaypoint(ctx);
      }

      @Override
      public CommandResult visitUnsetServerWaypoint(JourneyParser.UnsetServerWaypointContext ctx) {
        return super.visitUnsetServerWaypoint(ctx);
      }

      @Override
      public CommandResult visitRenameServerWaypoint(JourneyParser.RenameServerWaypointContext ctx) {
        return super.visitRenameServerWaypoint(ctx);
      }

      @Override
      public CommandResult visitQuest(JourneyParser.QuestContext ctx) {
        return super.visitQuest(ctx);
      }

      @Override
      public CommandResult visitSurface(JourneyParser.SurfaceContext ctx) {
        return super.visitSurface(ctx);
      }

      @Override
      public CommandResult visitDeath(JourneyParser.DeathContext ctx) {
        return super.visitDeath(ctx);
      }

      @Override
      public CommandResult visitCancel(JourneyParser.CancelContext ctx) {
        return super.visitCancel(ctx);
      }

      @Override
      public CommandResult visitAccept(JourneyParser.AcceptContext ctx) {
        return super.visitAccept(ctx);
      }

      @Override
      public CommandResult visitIdentifier(JourneyParser.IdentifierContext ctx) {
        return super.visitIdentifier(ctx);
      }

      @Override
      public CommandResult visitPublicWaypoint(JourneyParser.PublicWaypointContext ctx) {
        return super.visitPublicWaypoint(ctx);
      }

      @Override
      public CommandResult visitAdmin(JourneyParser.AdminContext ctx) {
        return super.visitAdmin(ctx);
      }

      @Override
      public CommandResult visitTimeoutFlag(JourneyParser.TimeoutFlagContext ctx) {
        int timeout;
        if (ctx.timeout.getText().isEmpty()) {
          timeout = Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
        } else {
          try {
            timeout = Integer.parseUnsignedInt(ctx.timeout.getText());
          } catch (NumberFormatException e) {
            src.audience().sendMessage(Formatter.error("Your animation timeout parameter could not be parsed"));
            return CommandResult.failure();
          }
        }
        flags.addValueFlag(Flags.ANIMATE, timeout);
        return super.visitTimeoutFlag(ctx);
      }

      @Override
      public CommandResult visitAnimateFlag(JourneyParser.AnimateFlagContext ctx) {
        int delay;
        if (ctx.delay.getText().isEmpty()) {
          delay = 50;  // 50 milliseconds default
        } else {
          try {
            delay = Integer.parseUnsignedInt(ctx.delay.getText());
          } catch (NumberFormatException e) {
            src.audience().sendMessage(Formatter.error("Your animation timeout parameter could not be parsed"));
            return CommandResult.failure();
          }
        }
        flags.addValueFlag(Flags.ANIMATE, delay);
        return super.visitAnimateFlag(ctx);
      }

      @Override
      public CommandResult visitNoflyFlag(JourneyParser.NoflyFlagContext ctx) {
        flags.addFlag(Flags.NO_FLY);
        return super.visitNoflyFlag(ctx);
      }

      @Override
      public CommandResult visitNodoorFlag(JourneyParser.NodoorFlagContext ctx) {
        flags.addFlag(Flags.NO_DOOR);
        return super.visitNodoorFlag(ctx);
      }

      @Override
      public CommandResult visitDigFlag(JourneyParser.DigFlagContext ctx) {
        flags.addFlag(Flags.DIG);
        return super.visitDigFlag(ctx);
      }

      protected CommandResult unimplemented() {
        src.audience().sendMessage(Formatter.error("This command is not yet implemented"));
        return CommandResult.failure();
      }
    };
  }
}
