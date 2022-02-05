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

package edu.whimc.journey.spigot.command;

import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.journey.spigot.command.to.JourneyToMyCommand;
import edu.whimc.journey.spigot.command.to.JourneyToPublicCommand;
import edu.whimc.journey.spigot.command.to.JourneyToQuestCommand;
import edu.whimc.journey.spigot.command.to.JourneyToSurfaceCommand;
import edu.whimc.journey.spigot.search.PlayerDestinationGoalSearchSession;
import me.blackvein.quests.Quests;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * A command to allow the calculation of a path to some destination endpoint.
 *
 * @see edu.whimc.journey.common.search.SearchSession
 * @see PlayerDestinationGoalSearchSession
 */
public class JourneyToCommand extends FunctionlessCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyToCommand(@Nullable CommandNode parent) {
    super(parent, null,
        "Commands for navigating to certain locations",
        "to");
    addChildren(new JourneyToPublicCommand(this));
    addChildren(new JourneyToMyCommand(this));
    addChildren(new JourneyToSurfaceCommand(this));

    // Quests plugin
    Plugin questsPlugin = Bukkit.getPluginManager().getPlugin("Quests");
    if (questsPlugin instanceof Quests) {
      addChildren(new JourneyToQuestCommand(this, (Quests) questsPlugin));
    }
  }

}
