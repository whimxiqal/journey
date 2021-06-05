/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.indicator.spigot.command;

import com.google.common.collect.Lists;
import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.path.Endpoint;
import edu.whimc.indicator.common.path.ModeType;
import edu.whimc.indicator.common.path.Step;
import edu.whimc.indicator.config.Settings;
import edu.whimc.indicator.spigot.command.common.CommandError;
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.command.common.Parameter;
import edu.whimc.indicator.spigot.journey.PlayerJourney;
import edu.whimc.indicator.spigot.search.IndicatorSearch;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.common.path.ModeTypes;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class TrailCommand extends CommandNode {

  private static final int ONE_SECOND = 20;

  private static final int ILLUMINATED_COUNT = 16;
  private static final int TICKS_PER_PARTICLE = 3;
  private static final double CHANCE_OF_PARTICLE = 0.4;

  private static final List<CommandNode> extraChildren = new LinkedList<>();

  public TrailCommand() {
    super(null,
        Permissions.TRAIL_PERMISSION,
        "View your current activated trail",
        "trail");
    addSubcommand(Parameter.builder()
            .flags(Lists.newArrayList("nofly"))
            .supplier(Parameter.ParameterSupplier.builder()
                .strict(false)
                .usage("[timeout]")
                .build())
            .build(),
        "Specify the number of seconds to wait for a response");
    addChildren(new TrailCustomCommand(this));
    addChildren(extraChildren.toArray(new CommandNode[0]));
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args,
                                  @NotNull Set<String> flags) {

    if (!(sender instanceof Player)) {
      sendCommandError(sender, CommandError.ONLY_PLAYER);
      return false;
    }
    Player player = (Player) sender;

    player.sendMessage("Trail command!");
    return true;
  }

  public static boolean blazeTrailTo(@NotNull Player player,
                                     @NotNull LocationCell endpoint,
                                     @NotNull Set<String> flags,
                                     int timeout) {

    if (Indicator.getInstance().getJourneyManager().isSearching(player.getUniqueId())) {
      player.sendMessage(Format.error("Please wait until your search is over before performing a new one."));
      return false;
    }

    UUID playerUuid = player.getUniqueId();

    final long finalTimeout = timeout;
    IndicatorSearch search = new IndicatorSearch(player, flags.contains("nofly"));

    // Set up a search cancellation in case it takes too long
    BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(Indicator.getInstance(),
        search::cancel,
        finalTimeout * ONE_SECOND);

    // Set up a "Working..." message if it takes too long
    BukkitTask workingNotification = Bukkit.getScheduler().runTaskLater(Indicator.getInstance(), () ->
        player.sendMessage(Format.info("Searching for path to your destination (" + finalTimeout + " sec)...")), 10);

    AtomicBoolean foundPath = new AtomicBoolean(false);
    AtomicInteger successNotificationTaskId = new AtomicInteger(0);
    AtomicBoolean sentSuccessNotification = new AtomicBoolean(false);

    // Set behavior for if the search works
    Random rand = new Random();
    search.setFoundNewOptimalPathEvent(path -> {

      if (sentSuccessNotification.get()) {
        // TODO do something different for subsequent found paths
        player.sendMessage(Format.info("A faster path to your destination was found..."));
        player.sendMessage(Format.info("You may use this feature in later versions."));
        return;
      }

      if (foundPath.get()) {
        Bukkit.getScheduler().cancelTask(successNotificationTaskId.get());
      }
      foundPath.set(true);

      // Cancel "Working..." message if it hasn't happened yet
      workingNotification.cancel();

      // Set up illumination scheduled task for showing the trails
      int illuminationTaskId = Bukkit.getScheduler().runTaskTimer(Indicator.getInstance(), () -> {
        Optional<PlayerJourney> journeyOptional = Indicator.getInstance()
            .getJourneyManager()
            .getPlayerJourney(playerUuid);
        if (!journeyOptional.isPresent()) return;
        PlayerJourney journey = journeyOptional.get();

        List<Step<LocationCell, World>> steps = journey.next(ILLUMINATED_COUNT);  // Show 16 steps ahead
        for (int i = 0; i < steps.size() - 1; i++) {
          if (rand.nextDouble() < CHANCE_OF_PARTICLE) {  // make shimmering effect
            Particle particle;
            ModeType modeType = steps.get(i + 1).getModeType();
            if (modeType.equals(ModeTypes.WALK)) {
              particle = Particle.FLAME;
            } else if (modeType.equals(ModeTypes.JUMP)) {
              particle = Particle.HEART;
            } else {
              particle = Particle.CLOUD;
            }
            steps.get(i).getLocatable().getDomain().spawnParticle(particle,
                steps.get(i).getLocatable().getX() + rand.nextDouble(),
                steps.get(i).getLocatable().getY() + 0.4f,
                steps.get(i).getLocatable().getZ() + rand.nextDouble(),
                1,
                0, 0, 0,
                0);
          }
        }
      }, 0, TICKS_PER_PARTICLE).getTaskId();

      // Create a journey that is completed when the player reaches within 3 blocks of the endpoint
      PlayerJourney journey = new PlayerJourney(playerUuid, path, cell -> {
        boolean completed = cell.distanceToSquared(endpoint) < 9;
        if (completed) {
          if (!search.isDone()) {
            // No need to search anything anymore
            search.cancel();
          }
        }
        return completed;
      });
      journey.setIlluminationTaskId(illuminationTaskId);

      // Save the journey and stop the illumination from the other one
      Indicator.getInstance().getJourneyManager().putPlayerJourney(playerUuid, journey);

      // Set up a success notification that will be cancelled if a better one is found in some amount of time
      successNotificationTaskId.set(Bukkit.getScheduler()
          .runTaskLater(Indicator.getInstance(),
              () -> {
                player.sendMessage(Format.success("Showing a path to your destination"));
                sentSuccessNotification.set(true);
              },
              ONE_SECOND / 2)
          .getTaskId());

    });

    // SEARCH
    Bukkit.getScheduler().runTaskAsynchronously(Indicator.getInstance(), () -> {
          // Add to the searching set so they can't search again
          Indicator.getInstance().getJourneyManager().startSearching(player.getUniqueId());
          // Search... this may take a long time
          search.search(new LocationCell(player.getLocation()), endpoint);

          // Cancel the timeout message if it hasn't happened yet
          timeoutTask.cancel();
          // Cancel "Working..." message if it hasn't happened yet
          workingNotification.cancel();
          // Send failure message if we finished unsuccessfully
          if (!search.isSuccessful()) {
            player.sendMessage(Format.error("A path to your destination could not be found."));
            Indicator.getInstance().getJourneyManager().removePlayerJourney(playerUuid);
          }
          // Remove from the searching set so they can search again
          Indicator.getInstance().getJourneyManager().stopSearching(player.getUniqueId());
        }
    );

    return true;
  }

  public static void registerChild(CommandNode commandNode) {
    extraChildren.add(commandNode);
  }

  public static int getTimeout(CommandSender src, String[] args, int timeoutIndex) {
    if (timeoutIndex >= args.length) {
      return Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
    }
    int timeout;
    try {
      timeout = Integer.parseInt(args[timeoutIndex]);
    } catch (NumberFormatException e) {
      src.sendMessage(Format.error("The timeout could not be converted to an integer"));
      return Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
    }

    if (timeout < 1) {
      src.sendMessage(Format.error("The timeout must be at least 1 second"));
      return Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
    } else if (timeout > 600) {
      src.sendMessage(Format.error("The timeout must be at most 10 minutes"));
      return Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
    }
    return timeout;
  }

}
