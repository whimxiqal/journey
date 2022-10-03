package me.pietelite.journey.common.command;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.JourneyBaseVisitor;
import me.pietelite.journey.common.JourneyParser;
import me.pietelite.journey.common.manager.DebugManager;
import me.pietelite.journey.common.message.Formatter;
import me.pietelite.mantle.common.CommandExecutor;
import me.pietelite.mantle.common.CommandResult;
import me.pietelite.mantle.common.CommandSource;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public class JourneyExecutor implements CommandExecutor {

  @Override
  public ParseTreeVisitor<CommandResult> provide(CommandSource src) {
    return new JourneyBaseVisitor<CommandResult>() {

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
        return super.visitSetwaypoint(ctx);
      }

      @Override
      public CommandResult visitListwaypoints(JourneyParser.ListwaypointsContext ctx) {
        return super.visitListwaypoints(ctx);
      }

      @Override
      public CommandResult visitWaypoint(JourneyParser.WaypointContext ctx) {
        return super.visitWaypoint(ctx);
      }

      @Override
      public CommandResult visitUnsetWaypoint(JourneyParser.UnsetWaypointContext ctx) {
        return super.visitUnsetWaypoint(ctx);
      }

      @Override
      public CommandResult visitRenameWaypoint(JourneyParser.RenameWaypointContext ctx) {
        return super.visitRenameWaypoint(ctx);
      }

      @Override
      public CommandResult visitPlayer(JourneyParser.PlayerContext ctx) {
        return super.visitPlayer(ctx);
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
    };
  }
}
