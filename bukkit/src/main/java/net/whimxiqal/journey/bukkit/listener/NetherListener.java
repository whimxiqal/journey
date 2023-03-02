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

package net.whimxiqal.journey.bukkit.listener;

import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class NetherListener implements Listener {

  /**
   * An event handler for when an entity goes through a portal.
   * In this case, we know for sure how a portal is linked, and it can be saved.
   *
   * @param e the event
   */
  @EventHandler(priority = EventPriority.LOW)
  public void onEntityPortal(EntityPortalEvent e) {
    Journey.get().netherManager().lookForPortal(BukkitUtil.cell(e.getFrom()), () -> BukkitUtil.cell(e.getEntity().getLocation()));
  }

  /**
   * An event handler for when a player goes through a portal.
   * In this case, we know for sure how a portal is linked, and it can be saved.
   *
   * @param e the event
   */
  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerPortal(PlayerPortalEvent e) {
    Journey.get().netherManager().lookForPortal(BukkitUtil.cell(e.getFrom()), () -> BukkitUtil.cell(e.getPlayer().getLocation()));
  }

}
