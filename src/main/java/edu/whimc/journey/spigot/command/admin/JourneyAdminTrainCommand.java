/*
 * MIT License
 *
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
 *
 */

package edu.whimc.journey.spigot.command.admin;

import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.common.data.DataAccessException;
import edu.whimc.journey.common.data.PathReportManager;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.Parameter;
import edu.whimc.journey.spigot.command.common.ParameterSuppliers;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JourneyAdminTrainCommand extends CommandNode {
  public JourneyAdminTrainCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.ADMIN,
        "Train some data for some amount of time",
        "train");
    addSubcommand(Parameter.builder()
        .supplier(ParameterSuppliers.INTEGER)
        .build(),
        "Specify how long we are supposed to train");
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args,
                                  @NotNull Map<String, String> flags) throws DataAccessException {
    List<PathReportManager.PathTrialRecord> records = new LinkedList<>();
    PathReportManager.PathTrialRecord record = JourneySpigot.getInstance()
        .getDataManager()
        .getPathReportManager()
        .getNextReport();
    while (record != null) {
      records.add(record);
      record = JourneySpigot.getInstance()
          .getDataManager()
          .getPathReportManager()
          .getNextReport();
    }

    int duration = 5000;  // 5 seconds
    if (args.length > 0) {
      int param = Integer.parseInt(args[0]);
      if (param < 5 || param > 5*60 /* 5 minutes */) {
        sender.spigot().sendMessage(Format.error("Your duration input is out of bounds! Using 5 seconds..."));
        return false;
      }
      duration = param * 1000;  // Convert to milliseconds
    }
    JourneyCommon.getNeuralNetwork().train(records, duration);
    return true;
  }
}
