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

package net.whimxiqal.journey.bukkit.search.listener;

import java.util.stream.Collectors;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.data.DataAccessException;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.search.FlexiblePathTrial;
import net.whimxiqal.journey.search.PathTrial;
import net.whimxiqal.journey.bukkit.search.event.BukkitStopPathSearchEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * A listener for managing data storage as operations throughout
 * this plugin proceed.
 */
public class DataStorageListener implements Listener {

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
    FlexiblePathTrial flexiblePathTrial = event.getSearchEvent().getPathTrial();
    if (flexiblePathTrial instanceof PathTrial) {
      PathTrial pathTrial = (PathTrial) flexiblePathTrial;
      if (pathTrial.getState().isSuccessful()) {
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
  }

}
