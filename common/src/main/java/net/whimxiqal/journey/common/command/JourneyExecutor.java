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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.whimxiqal.journey.common.Journey;
import net.whimxiqal.journey.common.JourneyBaseVisitor;
import net.whimxiqal.journey.common.JourneyParser;
import net.whimxiqal.journey.common.data.PersonalWaypointManager;
import net.whimxiqal.journey.common.data.PublicWaypointManager;
import net.whimxiqal.journey.common.integration.Scope;
import net.whimxiqal.journey.common.integration.ScopedLocationResult;
import net.whimxiqal.journey.common.manager.DebugManager;
import net.whimxiqal.journey.common.message.Formatter;
import net.whimxiqal.journey.common.message.Pager;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.search.PlayerDestinationGoalSearchSession;
import net.whimxiqal.journey.common.search.PlayerSurfaceGoalSearchSession;
import net.whimxiqal.journey.common.search.flag.FlagSet;
import net.whimxiqal.journey.common.search.flag.Flags;
import net.whimxiqal.journey.common.util.Request;
import net.whimxiqal.journey.common.util.Validator;
import net.whimxiqal.mantle.common.CommandContext;
import net.whimxiqal.mantle.common.CommandExecutor;
import net.whimxiqal.mantle.common.CommandResult;
import net.whimxiqal.mantle.common.CommandSource;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public class  JourneyExecutor implements CommandExecutor {

  public static final String PERSONAL_WAYPOINT_SCOPE = "personal";
  public static final String PUBLIC_WAYPOINT_SCOPE = "server";

  @Override
  public ParseTreeVisitor<CommandResult> provide(CommandContext cmd) {
    CommandSource src = cmd.source();
    return new JourneyBaseVisitor<CommandResult>() {
      final FlagSet flags = new FlagSet();

      @Override
      protected CommandResult aggregateResult(CommandResult aggregate, CommandResult nextResult) {
        if (aggregate != null) {
          return aggregate;
        }
        return nextResult;
      }

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
      public CommandResult visitJourneytoTarget(JourneyParser.JourneytoTargetContext ctx) {
        if (src.type() != CommandSource.Type.PLAYER) {
          src.audience().sendMessage(Formatter.error("Only players may execute this command"));
          return CommandResult.failure();
        }
        visitChildren(ctx);  // populate cmd.identifiers() and flags
        String scopedText = String.join(":", cmd.identifiers().getAll());
        ScopedLocationResult result = Scope.root(src).location(scopedText);
        switch (result.type()) {
          case EXISTS:
            return waypointSearch(result.location().get());
          case AMBIGUOUS:
            src.audience().sendMessage(Formatter.error("That name is ambiguous between scopes ___ and ___", result.ambiguousItem().get().scope1, result.ambiguousItem().get().scope2));
            return CommandResult.failure();
          case NO_SCOPE:
            src.audience().sendMessage(Formatter.error("Could not find a scope named ___", result.missing().get()));
          case NONE:
          default:
            src.audience().sendMessage(Formatter.error("Could not find that name under the given scope"));
            return CommandResult.failure();
        }
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
            if (ctx.target == null) {
              if (mgr.isDebugging(src.uuid())) {
                mgr.stopDebugging(src.uuid());
                src.audience().sendMessage(Formatter.success("Debug mode ___.", "disabled"));
              } else {
                mgr.startDebuggingAll(src.uuid());
                src.audience().sendMessage(Formatter.success("Debug mode ___ on all users.", "enabled"));
              }
            } else {
              Optional<UUID> target = Journey.get().proxy().platform().onlinePlayer(ctx.target.getText());
              if (!target.isPresent()) {
                src.audience().sendMessage(Formatter.error("Could not find that player"));
                return CommandResult.failure();
              }
              mgr.startDebuggingPlayer(src.uuid(), target.get());
              src.audience().sendMessage(Formatter.success("Debug mode ___ on ___.", "enabled ", ctx.target.getText()));
            }
            return CommandResult.success();
          default:
            return CommandResult.failure();
        }
      }

      @Override
      public CommandResult visitSetwaypoint(JourneyParser.SetwaypointContext ctx) {
        if (src.type() != CommandSource.Type.PLAYER) {
          src.audience().sendMessage(Formatter.error("Only players may execute this command"));
          return CommandResult.failure();
        }
        visitChildren(ctx);
        String name = cmd.identifiers().get(0);
        if (Validator.isInvalidDataName(name)) {
          src.audience().sendMessage(Formatter.error("That name is invalid"));
          return CommandResult.failure();
        }

        PersonalWaypointManager personalWaypointManager = Journey.get().dataManager().personalWaypointManager();

        Optional<Cell> location = Journey.get().proxy().platform().entityCellLocation(src.uuid());
        if (!location.isPresent()) {
          src.audience().sendMessage(Formatter.error("Your location could not be found"));
          return CommandResult.failure();
        }
        String existingName = personalWaypointManager.getName(src.uuid(), location.get());
        if (existingName != null) {
          src.audience().sendMessage(Formatter.error("Waypoint ___ already exists at this location", existingName));
          return CommandResult.failure();
        }

        if (personalWaypointManager.hasWaypoint(src.uuid(), name)) {
          src.audience().sendMessage(Formatter.error("A waypoint called ___ already exists", ctx.name.getText()));
          return CommandResult.failure();
        }

        personalWaypointManager.add(src.uuid(), location.get(), name);
        src.audience().sendMessage(Formatter.success("Set waypoint ___", name));
        return CommandResult.success();
      }

      @Override
      public CommandResult visitListwaypoints(JourneyParser.ListwaypointsContext ctx) {
        int page;
        if (ctx.page == null) {
          page = 1;
        } else {
          try {
            page = Integer.parseUnsignedInt(ctx.page.getText());
          } catch (NumberFormatException e) {
            src.audience().sendMessage(Formatter.error("Page number is invalid"));
            return CommandResult.failure();
          }
        }

        Map<String, Cell> cells = Journey.get().dataManager()
            .personalWaypointManager()
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
        CommandResult childrenResult = visitChildren(ctx);
        if (childrenResult != null) {
          return childrenResult;
        }
        // No other command specified, so journey to the waypoint
        return personalWaypointSearch(cmd.identifiers().get(0));
      }

      private CommandResult personalWaypointSearch(String name) {
        PersonalWaypointManager personalWaypointManager = Journey.get().dataManager().personalWaypointManager();
        Cell endLocation = personalWaypointManager.getWaypoint(src.uuid(), name);

        if (endLocation == null) {
          src.audience().sendMessage(Formatter.error("Could not find a waypoint called ___", name));
          return CommandResult.failure();
        }
        return waypointSearch(endLocation);
      }

      private CommandResult publicWaypointSearch(String name) {
        PublicWaypointManager personalWaypointManager = Journey.get().dataManager().publicWaypointManager();
        Cell endLocation = personalWaypointManager.getWaypoint(name);

        if (endLocation == null) {
          src.audience().sendMessage(Formatter.error("Could not find a public waypoint called ___", name));
          return CommandResult.failure();
        }
        return waypointSearch(endLocation);
      }

      private CommandResult waypointSearch(Cell endLocation) {
        Optional<Cell> location = Journey.get().proxy().platform().entityCellLocation(src.uuid());
        if (!location.isPresent()) {
          src.audience().sendMessage(Formatter.error("Your location could not be found"));
          return CommandResult.failure();
        }
        PlayerDestinationGoalSearchSession session = new PlayerDestinationGoalSearchSession(src.uuid(), location.get(), endLocation, flags, true);

        Journey.get().searchManager().launchSearch(session);
        return CommandResult.success();
      }


      @Override
      public CommandResult visitUnsetWaypoint(JourneyParser.UnsetWaypointContext ctx) {
        PersonalWaypointManager waypointManager = Journey.get().dataManager().personalWaypointManager();
        String name = cmd.identifiers().get(0);
        if (waypointManager.hasWaypoint(src.uuid(), name)) {
          waypointManager.remove(src.uuid(), name);
          src.audience().sendMessage(Formatter.success("Waypoint ___ has been removed", name));
          return CommandResult.success();
        } else {
          src.audience().sendMessage(Formatter.error("Waypoint ___ could not be found", name));
          return CommandResult.failure();
        }
      }

      @Override
      public CommandResult visitRenameWaypoint(JourneyParser.RenameWaypointContext ctx) {
        if (src.type() != CommandSource.Type.PLAYER) {
          src.audience().sendMessage(Formatter.error("Only players may execute this command"));
          return CommandResult.failure();
        }
        visitChildren(ctx);
        String name = cmd.identifiers().get(0);
        if (Validator.isInvalidDataName(name)) {
          src.audience().sendMessage(Formatter.error("That name is invalid"));
          return CommandResult.failure();
        }

        String newName = cmd.identifiers().get(1);
        if (Validator.isInvalidDataName(newName)) {
          src.audience().sendMessage(Formatter.error("That name is invalid"));
          return CommandResult.failure();
        }

        PersonalWaypointManager personalWaypointManager = Journey.get().dataManager().personalWaypointManager();

        if (personalWaypointManager.hasWaypoint(src.uuid(), newName)) {
          src.audience().sendMessage(Formatter.error("A waypoint called ___ already exists", ctx.newname.getText()));
          return CommandResult.failure();
        }

        personalWaypointManager.renameWaypoint(src.uuid(), name, newName);
        src.audience().sendMessage(Formatter.success("Renamed waypoint ___ to ___", name, newName));
        return CommandResult.success();
      }

      @Override
      public CommandResult visitPlayer(JourneyParser.PlayerContext ctx) {
        if (src.type() != CommandSource.Type.PLAYER) {
          src.audience().sendMessage(Formatter.error("Only players may execute this command"));
          return CommandResult.failure();
        }
        CommandResult childrenResult = visitChildren(ctx);
        if (childrenResult != null) {
          return childrenResult;
        }
        Optional<UUID> maybePlayer = Journey.get().proxy().platform().onlinePlayer(cmd.identifiers().get(0));
        if (!maybePlayer.isPresent()) {
          src.audience().sendMessage(Formatter.noPlayer(ctx.user.getText()));
          return CommandResult.failure();
        }
        if (maybePlayer.get().equals(src.uuid())) {
          src.audience().sendMessage(Formatter.error("You may not travel to yourself"));
          return CommandResult.failure();
        }
        Optional<Cell> location = Journey.get().proxy().platform().entityCellLocation(src.uuid());
        if (!location.isPresent()) {
          src.audience().sendMessage(Formatter.error("Your location could not be found"));
          return CommandResult.failure();
        }
        Optional<Cell> otherLocation = Journey.get().proxy().platform().entityCellLocation(maybePlayer.get());
        if (!otherLocation.isPresent()) {
          src.audience().sendMessage(Formatter.error("The other player's location could not be found"));
          return CommandResult.failure();
        }
        PlayerDestinationGoalSearchSession session = new PlayerDestinationGoalSearchSession(src.uuid(), location.get(), otherLocation.get(), flags, false);

        Journey.get().searchManager().launchSearch(session);
        return CommandResult.success();
      }

      @Override
      public CommandResult visitPlayerWaypoint(JourneyParser.PlayerWaypointContext ctx) {
        // source is player
        visitChildren(ctx);
        String playerName = cmd.identifiers().get(0);
        String waypoint = cmd.identifiers().get(1);
        Optional<UUID> maybePlayer = Journey.get().proxy().platform().onlinePlayer(cmd.identifiers().get(0));
        if (maybePlayer.isPresent()) {
          return visitPlayerWaypoint(ctx, playerName, maybePlayer.get(), waypoint);
        }

        // Async call for the player uuid
        Request.getPlayerUuid(cmd.identifiers().get(0)).thenAccept(p -> {
          if (p == null) {
            src.audience().sendMessage(Formatter.error("A problem occurred trying to access that player's information"));
          } else {
            visitPlayerWaypoint(ctx, playerName, p, waypoint);
          }
        });
        return CommandResult.success();
      }

      private CommandResult visitPlayerWaypoint(JourneyParser.PlayerWaypointContext ctx, String playerName, UUID dstPlayer, String waypoint) {
        PersonalWaypointManager manager = Journey.get().dataManager().personalWaypointManager();
        if (!manager.hasWaypoint(dstPlayer, waypoint)) {
          src.audience().sendMessage(Formatter.error("Player ___ has no waypoint ___", playerName, waypoint));
          return CommandResult.failure();
        }
        if (!manager.isPublic(dstPlayer, waypoint) && !dstPlayer.equals(src.uuid())) {
          src.audience().sendMessage(Formatter.error("Player ___ has waypoint ___ set as private", playerName, waypoint));
          return CommandResult.failure();
        }

        waypointSearch(manager.getWaypoint(dstPlayer, waypoint));
        return CommandResult.success();
      }

      @Override
      public CommandResult visitServerSetWaypoint(JourneyParser.ServerSetWaypointContext ctx) {
        if (src.type() != CommandSource.Type.PLAYER) {
          src.audience().sendMessage(Formatter.error("Only players may execute this command"));
          return CommandResult.failure();
        }
        visitChildren(ctx);
        String name = cmd.identifiers().get(0);
        if (Validator.isInvalidDataName(name)) {
          src.audience().sendMessage(Formatter.error("That name is invalid"));
          return CommandResult.failure();
        }

        PublicWaypointManager publicWaypointManager = Journey.get().dataManager().publicWaypointManager();

        Optional<Cell> location = Journey.get().proxy().platform().entityCellLocation(src.uuid());
        if (!location.isPresent()) {
          src.audience().sendMessage(Formatter.error("Your location could not be found"));
          return CommandResult.failure();
        }
        String existingName = publicWaypointManager.getName(location.get());
        if (existingName != null) {
          src.audience().sendMessage(Formatter.error("Waypoint ___ already exists at this location", existingName));
          return CommandResult.failure();
        }

        if (publicWaypointManager.hasWaypoint(name)) {
          src.audience().sendMessage(Formatter.error("A waypoint called ___ already exists", ctx.name.getText()));
          return CommandResult.failure();
        }

        publicWaypointManager.add(location.get(), name);
        src.audience().sendMessage(Formatter.success("Set waypoint ___", name));
        return CommandResult.success();
      }

      @Override
      public CommandResult visitServerListWaypoints(JourneyParser.ServerListWaypointsContext ctx) {
        int page;
        if (ctx.page == null) {
          page = 1;
        } else {
          try {
            page = Integer.parseUnsignedInt(ctx.page.getText());
          } catch (NumberFormatException e) {
            src.audience().sendMessage(Formatter.error("Page number is invalid"));
            return CommandResult.failure();
          }
        }

        Map<String, Cell> cells = Journey.get().dataManager().publicWaypointManager().getAll();

        if (cells.isEmpty()) {
          src.audience().sendMessage(Formatter.warn("There are no saved public waypoints yet!"));
          return CommandResult.success();
        }

        List<Map.Entry<String, Cell>> sortedEntryList = new ArrayList<>(cells.entrySet());
        sortedEntryList.sort(Map.Entry.comparingByKey());
        Pager.of(sortedEntryList, Map.Entry::getKey, entry -> entry.getValue().toString())
            .sendPage(src.audience(), page);

        return CommandResult.success();
      }

      @Override
      public CommandResult visitServerWaypoint(JourneyParser.ServerWaypointContext ctx) {
        CommandResult childrenResult = visitChildren(ctx);
        if (childrenResult != null) {
          return childrenResult;
        }

        // check for non-player type. We can't do this at framework-level because we want to allow
        // child commands
        if (src.type() != CommandSource.Type.PLAYER) {
          src.audience().sendMessage(Formatter.error("Only players may travel to a waypoint"));
          return CommandResult.failure();
        }
        // No other command specified, so journey to the waypoint
        return publicWaypointSearch(cmd.identifiers().get(0));
      }

      @Override
      public CommandResult visitUnsetServerWaypoint(JourneyParser.UnsetServerWaypointContext ctx) {
        PublicWaypointManager waypointManager = Journey.get().dataManager().publicWaypointManager();
        String name = cmd.identifiers().get(0);
        if (waypointManager.hasWaypoint(name)) {
          waypointManager.remove(name);
          src.audience().sendMessage(Formatter.success("Waypoint ___ has been removed", name));
          return CommandResult.success();
        } else {
          src.audience().sendMessage(Formatter.error("Waypoint ___ could not be found", name));
          return CommandResult.failure();
        }
      }

      @Override
      public CommandResult visitRenameServerWaypoint(JourneyParser.RenameServerWaypointContext ctx) {
        visitChildren(ctx);
        String name = cmd.identifiers().get(0);
        if (Validator.isInvalidDataName(name)) {
          src.audience().sendMessage(Formatter.error("That name is invalid"));
          return CommandResult.failure();
        }

        String newName = cmd.identifiers().get(1);
        if (Validator.isInvalidDataName(newName)) {
          src.audience().sendMessage(Formatter.error("That name is invalid"));
          return CommandResult.failure();
        }

        PublicWaypointManager publicWaypointManager = Journey.get().dataManager().publicWaypointManager();

        if (publicWaypointManager.hasWaypoint(newName)) {
          src.audience().sendMessage(Formatter.error("A waypoint called ___ already exists", newName));
          return CommandResult.failure();
        }

        publicWaypointManager.renameWaypoint(name, newName);
        src.audience().sendMessage(Formatter.success("Renamed waypoint ___ to ___", name, newName));
        return CommandResult.success();
      }

      @Override
      public CommandResult visitSurface(JourneyParser.SurfaceContext ctx) {
        visitChildren(ctx); // populate flags
        if (src.type() != CommandSource.Type.PLAYER) {
          src.audience().sendMessage(Formatter.error("Only players may execute this command"));
          return CommandResult.failure();
        }

        Optional<Cell> location = Journey.get().proxy().platform().entityCellLocation(src.uuid());
        if (!location.isPresent()) {
          src.audience().sendMessage(Formatter.error("Your location could not be found"));
          return CommandResult.failure();
        }

        PlayerSurfaceGoalSearchSession session = new PlayerSurfaceGoalSearchSession(src.uuid(), location.get(), flags);

        Journey.get().searchManager().launchSearch(session);
        return CommandResult.success();
      }

      @Override
      public CommandResult visitDeath(JourneyParser.DeathContext ctx) {
        visitChildren(ctx);  // populate flags
        if (src.type() != CommandSource.Type.PLAYER) {
          src.audience().sendMessage(Formatter.error("Only players may execute this command"));
          return CommandResult.failure();
        }
        CommandResult childrenResult = visitChildren(ctx);
        if (childrenResult != null) {
          return childrenResult;
        }
        // No other command specified, so journey to the waypoint

        Optional<Cell> endLocation = Journey.get().deathManager().getDeathLocation(src.uuid());
        if (!endLocation.isPresent()) {
          src.audience().sendMessage(Formatter.error("No known death location"));
          return CommandResult.failure();
        }

        Optional<Cell> location = Journey.get().proxy().platform().entityCellLocation(src.uuid());
        if (!location.isPresent()) {
          src.audience().sendMessage(Formatter.error("Your location could not be found"));
          return CommandResult.failure();
        }
        PlayerDestinationGoalSearchSession session = new PlayerDestinationGoalSearchSession(src.uuid(), location.get(), endLocation.get(), flags, false);

        Journey.get().searchManager().launchSearch(session);
        return CommandResult.success();
      }

      @Override
      public CommandResult visitCancel(JourneyParser.CancelContext ctx) {
        if (src.type() != CommandSource.Type.PLAYER) {
          src.audience().sendMessage(Formatter.error("Only players may execute this command"));
          return CommandResult.failure();
        }
        if (!Journey.get().searchManager().isSearching(src.uuid())) {
          src.audience().sendMessage(Formatter.error("You have no current search"));
          return CommandResult.failure();
        }
        Journey.get().searchManager().getSearch(src.uuid()).stop();
        src.audience().sendMessage(Formatter.info("Cancelling search..."));
        return CommandResult.success();
      }

      @Override
      public CommandResult visitPublicWaypoint(JourneyParser.PublicWaypointContext ctx) {
        if (src.type() != CommandSource.Type.PLAYER) {
          src.audience().sendMessage(Formatter.error("Only players may execute this command"));
          return CommandResult.failure();
        }

        String name = cmd.identifiers().get(0);

        PersonalWaypointManager personalWaypointManager = Journey.get().dataManager().personalWaypointManager();
        if (!personalWaypointManager.hasWaypoint(src.uuid(), name)) {
          src.audience().sendMessage(Formatter.error("No waypoint ___ exists", name));
        }

        boolean setTrue = ctx.TRUE() != null;
        boolean setFalse = ctx.FALSE() != null;
        boolean isPublic = personalWaypointManager.isPublic(src.uuid(), name);
        boolean result;
        if (setTrue || setFalse) {
          // only one of setTrue and setFalse may be true
          if (setTrue == isPublic) {
            src.audience().sendMessage(Formatter.warn("Waypoint ___ already has public status ___", name, setTrue ? "true" : "false"));
            return CommandResult.success();
          } else {
            personalWaypointManager.setPublic(src.uuid(), name, setTrue);
            result = setTrue;
          }
        } else {
          personalWaypointManager.setPublic(src.uuid(), name, !isPublic);
          result = !isPublic;
        }
        src.audience().sendMessage(Formatter.success("Waypoint ___ has been set with public status ___", name, result ? "true" : "false"));
        return CommandResult.success();
      }

      @Override
      public CommandResult visitAdmin(JourneyParser.AdminContext ctx) {
        if (ctx.invalidate != null) {
          Journey.get().dataManager().pathRecordManager().clear();
          Journey.get().netherManager().reset();
          src.audience().sendMessage(Formatter.success("Invalidate caches"));
          return CommandResult.success();
        }
        return super.visitAdmin(ctx);
      }

      @Override
      public CommandResult visitTimeoutFlag(JourneyParser.TimeoutFlagContext ctx) {
        int timeout;
        if (ctx.timeout == null) {
          return visitChildren(ctx);
        } else {
          try {
            timeout = Integer.parseUnsignedInt(ctx.timeout.getText());
          } catch (NumberFormatException e) {
            src.audience().sendMessage(Formatter.error("Your animation timeout parameter could not be parsed"));
            return CommandResult.failure();
          }
        }
        flags.addValueFlag(Flags.TIMEOUT, timeout);
        return super.visitTimeoutFlag(ctx);
      }

      @Override
      public CommandResult visitAnimateFlag(JourneyParser.AnimateFlagContext ctx) {
        int delay;
        if (ctx.delay == null) {
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
