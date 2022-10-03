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

package me.pietelite.journey.spigot.command.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.spigot.JourneySpigot;
import me.pietelite.journey.spigot.command.common.CommandNode;
import me.pietelite.journey.spigot.util.Format;
import me.pietelite.journey.spigot.util.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JourneyAdminListNetherPortalsCommand extends CommandNode {

  private final static int ITEMS_PER_PAGE = 10;

  public JourneyAdminListNetherPortalsCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.ADMIN, "Show all the known nether portals", "listnetherportals");
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args,
                                  @NotNull Map<String, String> flags) throws DataAccessException {
    ArrayList<NetherPort> ports = new ArrayList<>(JourneySpigot.getInstance().getNetherManager().makePorts());

    if (ports.isEmpty()) {
      sender.spigot().sendMessage(Format.debug("There are no saved nether portal connections."));
      return true;
    }

    int totalPages = (ports.size() / ITEMS_PER_PAGE + (ports.size() % ITEMS_PER_PAGE == 0 ? 0 : 1));
    int page = 1;
    if (args.length >= 1) {
      try {
        page = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        sender.spigot().sendMessage(Format.error("The page number was invalid"));
        return false;
      }
    }

    if (page > totalPages) {
      sender.spigot().sendMessage(Format.error("There are not that many pages"));
      return false;
    }

    int startList = (page - 1) * ITEMS_PER_PAGE;
    int endList = Math.min(page * ITEMS_PER_PAGE, ports.size());
    List<NetherPort> toShow = ports.subList(startList, endList);
    sender.spigot().sendMessage(Format.info("Nether Portal Connections, page " + page + "/" + totalPages));
    for (NetherPort port : toShow) {
      sender.spigot().sendMessage(Format.debug(port.getOrigin()
      + " -> "
      + port.getDestination()));
    }
    return true;
  }
}
