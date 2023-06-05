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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.InternalJourneyPlayer;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.common.JourneyBaseVisitor;
import net.whimxiqal.journey.common.JourneyParser;
import net.whimxiqal.journey.data.PersonalWaypointManager;
import net.whimxiqal.journey.data.PublicWaypointManager;
import net.whimxiqal.journey.data.TunnelType;
import net.whimxiqal.journey.data.Waypoint;
import net.whimxiqal.journey.manager.SearchManager;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.message.Pager;
import net.whimxiqal.journey.navigation.journey.PlayerJourneySession;
import net.whimxiqal.journey.scope.ScopeUtil;
import net.whimxiqal.journey.scope.ScopedSessionResult;
import net.whimxiqal.journey.search.DestinationGoalSearchSession;
import net.whimxiqal.journey.search.EverythingSearch;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.flag.FlagSet;
import net.whimxiqal.journey.search.flag.Flags;
import net.whimxiqal.journey.util.CommonLogger;
import net.whimxiqal.journey.util.Permission;
import net.whimxiqal.journey.util.Request;
import net.whimxiqal.journey.util.Validator;
import net.whimxiqal.mantle.common.CommandContext;
import net.whimxiqal.mantle.common.CommandExecutor;
import net.whimxiqal.mantle.common.CommandResult;
import net.whimxiqal.mantle.common.CommandSource;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.jetbrains.annotations.Nullable;

public class JourneyExecutor implements CommandExecutor {

  @Override
  public ParseTreeVisitor<CommandResult> provide(CommandContext cmd) {
    CommandSource src = cmd.source();
    return new JourneyBaseVisitor<>() {
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
      public CommandResult visitJourneyto(JourneyParser.JourneytoContext ctx) {
        CommandResult result = visitChildren(ctx);
        if (result != null) {
          return result;
        }
        if (notAllowed(Permission.PATH_GUI, true)) {
          return CommandResult.failure();
        }
        boolean sent = Journey.get().proxy().platform().sendGui(InternalJourneyPlayer.from(src));
        if (!sent) {
          src.audience().sendMessage(Formatter.error("The GUI is not implemented on this version"));
          return CommandResult.failure();
        }
        return CommandResult.success();
      }

      @Override
      public CommandResult visitJourneytoTarget(JourneyParser.JourneytoTargetContext ctx) {
        visitChildren(ctx);  // populate flags
        String scopedText = String.join(":", cmd.identifiers().getAll());

        ScopedSessionResult result = ScopeUtil.session(InternalJourneyPlayer.from(src), scopedText);
        switch (result.type()) {
          case EXISTS:
            SearchSession session = result.session().get();
            session.setFlags(flags);
            Journey.get().searchManager().launchIngameSearch(session);
            return CommandResult.success();
          case AMBIGUOUS:
            src.audience().sendMessage(Formatter.error("That name is ambiguous between scopes ___ and ___",
                result.ambiguousItem().get().scope1, result.ambiguousItem().get().scope2));
            return CommandResult.failure();
          case NO_SCOPE:
            src.audience().sendMessage(Formatter.error("Could not find a scope named ___",
                result.missing().get()));
          case NONE:
            src.audience().sendMessage(Formatter.error("Could not find anything with that name"));
            return CommandResult.failure();
          case NO_PERMISSION:
            src.audience().sendMessage(Formatter.error("You do not have permission to go there"));
            return CommandResult.failure();
          default:
            return CommandResult.failure();  // should never happen
        }
      }

      @Override
      public CommandResult visitDebug(JourneyParser.DebugContext ctx) {
        Component message;
        if (Journey.logger().level() == CommonLogger.LogLevel.DEBUG) {
          message = Formatter.success("Debug mode ___.", "disabled");
          Journey.logger().setLevel(CommonLogger.LogLevel.INFO);
        } else {
          message = Formatter.success("Debug mode ___.", "enabled");
          Journey.logger().setLevel(CommonLogger.LogLevel.DEBUG);
        }
        src.audience().sendMessage(message);
        if (src.type() != CommandSource.Type.CONSOLE) {
          // Also send the message to the console, if the source wasn't already the console
          Journey.get().proxy().audienceProvider().console().sendMessage(message);
        }
        return CommandResult.success();
      }

      @Override
      public CommandResult visitCachePortals(JourneyParser.CachePortalsContext ctx) {
        if (ctx.clear != null) {
          Journey.get().netherManager().reset().thenRun(() -> src.audience().sendMessage(Formatter.success("Cleared cached portals.")));
          return CommandResult.success();
        }
        return CommandResult.failure();
      }

      @Override
      public CommandResult visitCachePaths(JourneyParser.CachePathsContext ctx) {
        if (ctx.build != null) {
          src.audience().sendMessage(Formatter.success("Building path cache... See console for progress."));
          SearchSession.Caller callerType;
          UUID searcherUuid = null;
          switch (src.type()) {
            case PLAYER -> {
              callerType = SearchSession.Caller.PLAYER;
              searcherUuid = src.uuid();
            }
            case CONSOLE -> {
              callerType = SearchSession.Caller.CONSOLE;
              searcherUuid = SearchManager.CONSOLE_UUID;
            }
            default -> callerType = SearchSession.Caller.OTHER;
          }
          Journey.get().searchManager().launchIngameSearch(new EverythingSearch(searcherUuid, callerType));
          return CommandResult.success();
        } else if (ctx.clear != null) {
          Journey.get().proxy().schedulingManager().schedule(() -> {
            Journey.get().proxy().dataManager().pathRecordManager().truncate();
            src.audience().sendMessage(Formatter.success("Cleared cached paths."));
          }, true);
          return CommandResult.success();
        }
        return CommandResult.failure();
      }

      @Override
      public CommandResult visitListNetherPortals(JourneyParser.ListNetherPortalsContext ctx) {
        Optional<Integer> page = getPage(ctx.page);
        if (page.isEmpty()) {
          return CommandResult.failure();
        }
        Journey.get().proxy().schedulingManager().schedule(() -> Pager.of(Formatter.info("Known Nether Portals"),
                Journey.get().proxy().dataManager()
                    .netherPortalManager()
                    .getAllTunnels(TunnelType.NETHER),
                tunnel -> Formatter.cell(tunnel.origin()),
                tunnel -> Formatter.cell(tunnel.destination()))
            .sendPage(src.audience(), page.get()), true);
        return CommandResult.success();
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

        Optional<Cell> location = Journey.get().proxy().platform().entityCellLocation(src.uuid());
        if (location.isEmpty()) {
          src.audience().sendMessage(Formatter.error("Your location could not be found"));
          return CommandResult.failure();
        }
        PersonalWaypointManager personalWaypointManager = Journey.get().proxy().dataManager().personalWaypointManager();

        Journey.get().proxy().schedulingManager().schedule(() -> {
          String existingName = personalWaypointManager.getName(src.uuid(), location.get());
          if (existingName != null) {
            src.audience().sendMessage(Formatter.error("Waypoint ___ already exists at this location", existingName));
            return;
          }

          if (personalWaypointManager.hasWaypoint(src.uuid(), name)) {
            src.audience().sendMessage(Formatter.error("A waypoint called ___ already exists", ctx.name.getText()));
            return;
          }

          personalWaypointManager.add(src.uuid(), location.get(), name);
          Journey.get().cachedDataProvider().personalWaypointCache().update(src.uuid(), true);
          src.audience().sendMessage(Formatter.success("Set waypoint ___", name));
        }, true);
        return CommandResult.success();
      }

      @Override
      public CommandResult visitListwaypointsMine(JourneyParser.ListwaypointsMineContext ctx) {
        Optional<Integer> page = getPage(ctx.page);
        if (page.isEmpty()) {
          return CommandResult.failure();
        }

        Journey.get().proxy().schedulingManager().schedule(() -> {
          List<Waypoint> waypoints = new ArrayList<>(Journey.get().proxy().dataManager()
              .personalWaypointManager()
              .getAll(src.uuid(), false));

          if (waypoints.isEmpty()) {
            src.audience().sendMessage(Formatter.warn("You have no saved waypoints yet!"));
            return;
          }

          waypoints.sort(Comparator.naturalOrder());
          Pager.of(Formatter.info("Your Waypoints"), waypoints,
                  waypoint -> Component.text(waypoint.name()).color(Formatter.GOLD),
                  waypoint -> Formatter.cell(waypoint.location()))
              .sendPage(src.audience(), page.get());
        }, true);

        return CommandResult.success();
      }

      @Override
      public CommandResult visitListwaypointsPlayer(JourneyParser.ListwaypointsPlayerContext ctx) {
        Optional<Integer> page = getPage(ctx.page);
        if (page.isEmpty()) {
          return CommandResult.failure();
        }

        String playerName = cmd.identifiers().get(0);
        Optional<InternalJourneyPlayer> maybePlayer = Journey.get().proxy().platform().onlinePlayer(playerName);

        Consumer<UUID> runWithUuid = playerUuid -> {
          if (playerUuid == null) {
            src.audience().sendMessage(Formatter.error("A problem occurred trying to access that player's information"));
          } else {
            List<Waypoint> waypoints = new ArrayList<>(Journey.get().proxy().dataManager()
                .personalWaypointManager()
                .getAll(playerUuid, false));

            if (waypoints.isEmpty()) {
              src.audience().sendMessage(Formatter.warn("That player has no saved waypoints yet!"));
              return;
            }

            waypoints.sort(Comparator.naturalOrder());
            Pager.of(Formatter.info(playerName + "'s Waypoints"), waypoints,
                    waypoint -> Component.text(waypoint.name()).color(Formatter.GOLD),
                    waypoint -> Formatter.cell(waypoint.location()))
                .sendPage(src.audience(), page.get());
          }
        };

        if (maybePlayer.isPresent()) {
          Journey.get().proxy().schedulingManager().schedule(() -> runWithUuid.accept(maybePlayer.get().uuid()), true);
        } else {
          // Async call for the player uuid
          Request.getPlayerUuidAsync(cmd.identifiers().get(0)).thenAccept(runWithUuid);
        }
        return CommandResult.success();
      }

      @Override
      public CommandResult visitWaypoint(JourneyParser.WaypointContext ctx) {
        CommandResult childrenResult = visitChildren(ctx);
        if (childrenResult != null) {
          return childrenResult;
        }
        // No other command specified, so journey to the waypoint
        if (notAllowed(Permission.PATH_PERSONAL, true)) {
          return CommandResult.failure();
        }
        personalWaypointSearch(cmd.identifiers().get(0));
        return CommandResult.success();
      }

      private void personalWaypointSearch(String name) {
        Optional<Cell> location = Journey.get().proxy().platform().entityCellLocation(src.uuid());
        if (location.isEmpty()) {
          src.audience().sendMessage(Formatter.error("Your location could not be found"));
          return;
        }
        Journey.get().proxy().schedulingManager().schedule(() -> {
          Cell endLocation = Journey.get().proxy().dataManager().personalWaypointManager().getWaypoint(src.uuid(), name);

          if (endLocation == null) {
            src.audience().sendMessage(Formatter.error("Could not find a waypoint called ___", name));
            return;
          }
          destinationSearch(location.get(), endLocation);
        }, true);
      }

      private void publicWaypointSearch(String name) {
        Optional<Cell> location = Journey.get().proxy().platform().entityCellLocation(src.uuid());
        if (location.isEmpty()) {
          src.audience().sendMessage(Formatter.error("Your location could not be found"));
          return;
        }
        Journey.get().proxy().schedulingManager().schedule(() -> {
          Cell endLocation = Journey.get().proxy().dataManager().publicWaypointManager().getWaypoint(name);

          if (endLocation == null) {
            src.audience().sendMessage(Formatter.error("Could not find a public waypoint called ___", name));
            return;
          }
          destinationSearch(location.get(), endLocation);
        }, true);
      }

      private void destinationSearch(Cell startLocation, Cell endLocation) {
        for (int i = 0; i < 200; i++) {
          Journey.logger().info("Instantiating search");
          DestinationGoalSearchSession session = new DestinationGoalSearchSession(UUID.randomUUID(), SearchSession.Caller.PLAYER, startLocation, endLocation, false, true);
          session.setFlags(flags);

          Journey.get().searchManager().launchIngameSearch(session);
        }
      }

      @Override
      public CommandResult visitUnsetWaypoint(JourneyParser.UnsetWaypointContext ctx) {
        PersonalWaypointManager waypointManager = Journey.get().proxy().dataManager().personalWaypointManager();
        String name = cmd.identifiers().get(0);
        Journey.get().proxy().schedulingManager().schedule(() -> {
          if (waypointManager.hasWaypoint(src.uuid(), name)) {
            waypointManager.remove(src.uuid(), name);
            Journey.get().cachedDataProvider().personalWaypointCache().update(src.uuid(), true);
            src.audience().sendMessage(Formatter.success("Waypoint ___ has been removed", name));
          } else {
            src.audience().sendMessage(Formatter.error("Waypoint ___ could not be found", name));
          }
        }, true);
        return CommandResult.success();
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

        PersonalWaypointManager personalWaypointManager = Journey.get().proxy().dataManager().personalWaypointManager();

        Journey.get().proxy().schedulingManager().schedule(() -> {
          if (personalWaypointManager.hasWaypoint(src.uuid(), newName)) {
            src.audience().sendMessage(Formatter.error("A waypoint called ___ already exists", ctx.newname.getText()));
            return;
          }

          personalWaypointManager.renameWaypoint(src.uuid(), name, newName);
          Journey.get().cachedDataProvider().personalWaypointCache().update(src.uuid(), true);
          src.audience().sendMessage(Formatter.success("Renamed waypoint ___ to ___", name, newName));
        }, true);
        return CommandResult.success();
      }

      @Override
      public CommandResult visitPlayer(JourneyParser.PlayerContext ctx) {
        CommandResult childrenResult = visitChildren(ctx);
        if (childrenResult != null) {
          return childrenResult;
        }

        // No children commands run, so try go to the player themselves
        if (notAllowed(Permission.PATH_PLAYER_ENTITY, true)) {
          return CommandResult.failure();
        }
        Optional<InternalJourneyPlayer> maybePlayer = Journey.get().proxy().platform().onlinePlayer(cmd.identifiers().get(0));
        if (!maybePlayer.isPresent()) {
          src.audience().sendMessage(Formatter.noPlayer(ctx.user.getText()));
          return CommandResult.failure();
        }
        if (maybePlayer.get().uuid().equals(src.uuid())) {
          src.audience().sendMessage(Formatter.error("You may not travel to yourself"));
          return CommandResult.failure();
        }
        Optional<Cell> location = Journey.get().proxy().platform().entityCellLocation(src.uuid());
        if (!location.isPresent()) {
          src.audience().sendMessage(Formatter.error("Your location could not be found"));
          return CommandResult.failure();
        }
        Optional<Cell> otherLocation = Journey.get().proxy().platform().entityCellLocation(maybePlayer.get().uuid());
        if (!otherLocation.isPresent()) {
          src.audience().sendMessage(Formatter.error("The other player's location could not be found"));
          return CommandResult.failure();
        }
        DestinationGoalSearchSession session = new DestinationGoalSearchSession(src.uuid(), SearchSession.Caller.PLAYER, location.get(), otherLocation.get(), false, false);

        Journey.get().searchManager().launchIngameSearch(session);
        return CommandResult.success();
      }

      @Override
      public CommandResult visitPlayerWaypoint(JourneyParser.PlayerWaypointContext ctx) {
        // source is player
        visitChildren(ctx);
        String playerName = cmd.identifiers().get(0);
        String waypoint = cmd.identifiers().get(1);
        Optional<InternalJourneyPlayer> maybePlayer = Journey.get().proxy().platform().onlinePlayer(cmd.identifiers().get(0));
        if (maybePlayer.isPresent()) {
          visitPlayerWaypoint(ctx, playerName, maybePlayer.get().uuid(), waypoint);
          return CommandResult.success();
        }

        // Async call for the player uuid
        Request.getPlayerUuidAsync(cmd.identifiers().get(0)).thenAccept(p -> {
          if (p == null) {
            src.audience().sendMessage(Formatter.error("A problem occurred trying to access that player's information"));
          } else {
            visitPlayerWaypoint(ctx, playerName, p, waypoint);
          }
        });
        return CommandResult.success();
      }

      private void visitPlayerWaypoint(JourneyParser.PlayerWaypointContext ctx, String playerName, UUID dstPlayer, String waypoint) {
        Optional<Cell> location = Journey.get().proxy().platform().entityCellLocation(src.uuid());

        if (location.isEmpty()) {
          src.audience().sendMessage(Formatter.error("Your location could not be found"));
          return;
        }
        Journey.get().proxy().schedulingManager().schedule(() -> {
          PersonalWaypointManager manager = Journey.get().proxy().dataManager().personalWaypointManager();
          if (!manager.hasWaypoint(dstPlayer, waypoint)) {
            src.audience().sendMessage(Formatter.error("Player ___ has no waypoint ___", playerName, waypoint));
            return;
          }
          if (!manager.isPublic(dstPlayer, waypoint) && !dstPlayer.equals(src.uuid())) {
            src.audience().sendMessage(Formatter.error("Player ___ has waypoint ___ set as private", playerName, waypoint));
            return;
          }

          destinationSearch(location.get(), manager.getWaypoint(dstPlayer, waypoint));
        }, true);
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

        Optional<Cell> location = Journey.get().proxy().platform().entityCellLocation(src.uuid());

        if (location.isEmpty()) {
          src.audience().sendMessage(Formatter.error("Your location could not be found"));
          return CommandResult.failure();
        }

        Journey.get().proxy().schedulingManager().schedule(() -> {
          PublicWaypointManager publicWaypointManager = Journey.get().proxy().dataManager().publicWaypointManager();

          String existingName = publicWaypointManager.getName(location.get());
          if (existingName != null) {
            src.audience().sendMessage(Formatter.error("Waypoint ___ already exists at this location", existingName));
            return;
          }

          if (publicWaypointManager.hasWaypoint(name)) {
            src.audience().sendMessage(Formatter.error("A waypoint called ___ already exists", ctx.name.getText()));
            return;
          }

          publicWaypointManager.add(location.get(), name);
          Journey.get().cachedDataProvider().publicWaypointCache().update(true);
          src.audience().sendMessage(Formatter.success("Set waypoint ___", name));
        }, true);
        return CommandResult.success();
      }

      @Override
      public CommandResult visitServerListWaypoints(JourneyParser.ServerListWaypointsContext ctx) {
        Optional<Integer> page = getPage(ctx.page);
        if (page.isEmpty()) {
          return CommandResult.failure();
        }

        Journey.get().proxy().schedulingManager().schedule(() -> {
          List<Waypoint> waypoints = new ArrayList<>(Journey.get().proxy().dataManager().publicWaypointManager().getAll());

          if (waypoints.isEmpty()) {
            src.audience().sendMessage(Formatter.warn("There are no saved public waypoints yet!"));
            return;
          }

          waypoints.sort(Comparator.naturalOrder());
          Pager.of(Formatter.info("Server Waypoints"),
                  waypoints,
                  waypoint -> Component.text(waypoint.name()).color(Formatter.GOLD),
                  waypoint -> Formatter.cell(waypoint.location()))
              .sendPage(src.audience(), page.get());
        }, true);

        return CommandResult.success();
      }

      @Override
      public CommandResult visitServerWaypoint(JourneyParser.ServerWaypointContext ctx) {
        CommandResult childrenResult = visitChildren(ctx);
        if (childrenResult != null) {
          return childrenResult;
        }
        if (notAllowed(Permission.PATH_SERVER, true)) {
          return CommandResult.failure();
        }
        // No other command specified, so journey to the waypoint
        publicWaypointSearch(cmd.identifiers().get(0));
        return CommandResult.success();
      }

      @Override
      public CommandResult visitServerUnsetWaypoint(JourneyParser.ServerUnsetWaypointContext ctx) {
        PublicWaypointManager waypointManager = Journey.get().proxy().dataManager().publicWaypointManager();
        String name = cmd.identifiers().get(0);
        if (waypointManager.hasWaypoint(name)) {
          waypointManager.remove(name);
          Journey.get().cachedDataProvider().publicWaypointCache().update(true);
          src.audience().sendMessage(Formatter.success("Waypoint ___ has been removed", name));
          return CommandResult.success();
        } else {
          src.audience().sendMessage(Formatter.error("Waypoint ___ could not be found", name));
          return CommandResult.failure();
        }
      }

      @Override
      public CommandResult visitServerRenameWaypoint(JourneyParser.ServerRenameWaypointContext ctx) {
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

        PublicWaypointManager publicWaypointManager = Journey.get().proxy().dataManager().publicWaypointManager();

        if (publicWaypointManager.hasWaypoint(newName)) {
          src.audience().sendMessage(Formatter.error("A waypoint called ___ already exists", newName));
          return CommandResult.failure();
        }

        publicWaypointManager.renameWaypoint(name, newName);
        Journey.get().cachedDataProvider().publicWaypointCache().update(true);
        src.audience().sendMessage(Formatter.success("Renamed waypoint ___ to ___", name, newName));
        return CommandResult.success();
      }

      @Override
      public CommandResult visitCancel(JourneyParser.CancelContext ctx) {
        UUID searcherUuid;
        switch (src.type()) {
          case PLAYER -> searcherUuid = src.uuid();
          case CONSOLE -> searcherUuid = SearchManager.CONSOLE_UUID;
          default -> {
            src.audience().sendMessage(Formatter.error("You may not execute this command"));
            return CommandResult.failure();
          }
        }
        boolean canceled = false;
        SearchSession session = Journey.get().searchManager().getSearch(searcherUuid);
        if (session != null) {
          session.stop(true);
          src.audience().sendMessage(Formatter.info("Your search is being canceled"));
          canceled = true;
        }
        PlayerJourneySession journey = Journey.get().searchManager().getJourney(searcherUuid);
        if (journey != null && journey.running()) {
          journey.stop();
          src.audience().sendMessage(Formatter.info("Your path has been hidden"));
          canceled = true;
        }
        if (!canceled) {
          src.audience().sendMessage(Formatter.error("You have nothing to cancel"));
          return CommandResult.failure();
        }
        return CommandResult.success();
      }

      @Override
      public CommandResult visitPublicWaypoint(JourneyParser.PublicWaypointContext ctx) {
        if (src.type() != CommandSource.Type.PLAYER) {
          src.audience().sendMessage(Formatter.error("Only players may execute this command"));
          return CommandResult.failure();
        }

        String name = cmd.identifiers().get(0);

        PersonalWaypointManager personalWaypointManager = Journey.get().proxy().dataManager().personalWaypointManager();
        Journey.get().proxy().schedulingManager().schedule(() -> {
          if (!personalWaypointManager.hasWaypoint(src.uuid(), name)) {
            src.audience().sendMessage(Formatter.error("No waypoint ___ exists", name));
            return;
          }

          boolean setTrue = ctx.TRUE() != null;
          boolean setFalse = ctx.FALSE() != null;
          boolean isPublic = personalWaypointManager.isPublic(src.uuid(), name);
          boolean result;
          if (setTrue || setFalse) {
            // only one of setTrue and setFalse may be true
            if (setTrue == isPublic) {
              src.audience().sendMessage(Formatter.warn("Waypoint ___ already has public status ___", name, setTrue ? "true" : "false"));
              return;
            } else {
              personalWaypointManager.setPublic(src.uuid(), name, setTrue);
              Journey.get().cachedDataProvider().personalWaypointCache().update(src.uuid(), true);
              result = setTrue;
            }
          } else {
            personalWaypointManager.setPublic(src.uuid(), name, !isPublic);
            Journey.get().cachedDataProvider().personalWaypointCache().update(src.uuid(), true);
            result = !isPublic;
          }
          src.audience().sendMessage(Formatter.success("Waypoint ___ has been set with public status ___", name, result ? "true" : "false"));
        }, true);
        return CommandResult.success();
      }

      @Override
      public CommandResult visitAdmin(JourneyParser.AdminContext ctx) {
        if (ctx.reload != null) {
          if (notAllowed(Permission.ADMIN_RELOAD, false)) {
            return CommandResult.failure();
          }
          Journey.get().proxy().configManager().load();
          src.audience().sendMessage(Formatter.success("Reloaded config"));
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
        flags.addFlag(Flags.TIMEOUT, timeout);
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
        flags.addFlag(Flags.ANIMATE, delay);
        return super.visitAnimateFlag(ctx);
      }

      @Override
      public CommandResult visitFlyFlag(JourneyParser.FlyFlagContext ctx) {
        if (ctx.TRUE() != null) {
          flags.addFlag(Flags.FLY, true);
        } else if (ctx.FALSE() != null) {
          flags.addFlag(Flags.FLY, false);
        } else {
          flags.addFlag(Flags.FLY, !Flags.FLY.defaultValue());
        }
        return super.visitFlyFlag(ctx);
      }

      @Override
      public CommandResult visitDoorFlag(JourneyParser.DoorFlagContext ctx) {
        if (ctx.TRUE() != null) {
          flags.addFlag(Flags.DOOR, true);
        } else if (ctx.FALSE() != null) {
          flags.addFlag(Flags.DOOR, false);
        } else {
          flags.addFlag(Flags.DOOR, !Flags.DOOR.defaultValue());
        }
        return super.visitDoorFlag(ctx);
      }

      @Override
      public CommandResult visitDigFlag(JourneyParser.DigFlagContext ctx) {
        if (ctx.TRUE() != null) {
          flags.addFlag(Flags.DIG, true);
        } else if (ctx.FALSE() != null) {
          flags.addFlag(Flags.DIG, false);
        } else {
          flags.addFlag(Flags.DIG, !Flags.DIG.defaultValue());
        }
        return super.visitDigFlag(ctx);
      }

      private boolean notAllowed(@Nullable Permission permission, boolean onlyPlayer) {
        if (onlyPlayer && src.type() != CommandSource.Type.PLAYER) {
          src.audience().sendMessage(Formatter.error("Only players may execute this command"));
          return true;
        }
        if (permission != null && !src.hasPermission(permission.path())) {
          src.audience().sendMessage(Formatter.formattedMessage(Formatter.ERROR, "You do not have permission to execute this command", false));
          return true;
        }
        return false;
      }

      private Optional<Integer> getPage(Token pageToken) {
        if (pageToken == null) {
          return Optional.of(1);
        } else {
          try {
            return Optional.of(Integer.parseUnsignedInt(pageToken.getText()));
          } catch (NumberFormatException e) {
            src.audience().sendMessage(Formatter.error("Page number is invalid"));
            return Optional.empty();
          }
        }
      }
    };
  }
}
