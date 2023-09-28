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

package net.whimxiqal.journey.integration.citizens;

import java.io.IOException;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CitizensEventListener implements Listener {

  @EventHandler
  public void onCitizensEnable(CitizensEnableEvent event) {
    // We can only load the guide store after Citizens is fully enabled
    try {
      JourneyCitizens.get().guideStore().initialize();
    } catch (IOException e) {
      JourneyCitizens.logger().severe("Exception thrown trying to load known NPC guides");
      throw new RuntimeException(e);
    }
  }
}
