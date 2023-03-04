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

package net.whimxiqal.journey.bukkit.search.listener;

import java.util.stream.Collectors;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.bukkit.search.event.BukkitStopPathSearchEvent;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.data.DataAccessException;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.search.AbstractPathTrial;
import net.whimxiqal.journey.search.PathTrial;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * A listener for managing data storage as operations throughout
 * this plugin proceed.
 */
public class DataStorageListener implements Listener {

  private boolean loggedMaxCacheHit = false;

  /**
   * Catch the event that a path trial stopped so that we can save the data of the calculation
   * for the path of that path trial.
   *
   * @param event the event
   */
  @EventHandler
  public void savePathData(BukkitStopPathSearchEvent event) {
    if (!event.getSearchEvent().shouldSave()) {
      return;
    }
    AbstractPathTrial abstractPathTrial = event.getSearchEvent().getPathTrial();
    if (!(abstractPathTrial instanceof PathTrial pathTrial)) {
      return;
    }
    if (!pathTrial.getState().isStopped()) {
      throw new RuntimeException(); // programmer error
    }
    if (!pathTrial.getState().isSuccessful()) {
      return;
    }
    if (Journey.get().dataManager().pathRecordManager().totalRecordCellCount() + pathTrial.getLength() > Settings.MAX_CACHED_CELLS.getValue()) {
      if (!loggedMaxCacheHit) {
        Journey.logger().warn("The Journey database has cached the max number of cells allowed in the config file. Raise this number to continue caching results.");
        loggedMaxCacheHit = true;
      }
      return;
    }
    try {
      Journey.get().dataManager().pathRecordManager().report(
          pathTrial,
          event.getSearchEvent().getPathTrial().getModes().stream().map(Mode::type).collect(Collectors.toSet()),
          event.getSearchEvent().getExecutionTime());
    } catch (DataAccessException e) {
      e.printStackTrace();
    }
  }

}
