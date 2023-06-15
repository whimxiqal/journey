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

package net.whimxiqal.journey.data.cache;

import net.whimxiqal.journey.Journey;

public class CachedDataProvider {

  private final PersonalWaypointCache personalWaypointCache = new PersonalWaypointCache();
  private final PublicWaypointCache publicWaypointCache = new PublicWaypointCache();

  public void initialize() {
    personalWaypointCache().initialize();
    publicWaypointCache().initialize();
  }

  public void shutdown() {
    Journey.logger().debug("[Cached Data Provider] Shutting down...");
    personalWaypointCache().shutdown();
    // public waypoint cache does need to be shutdown
  }

  public PersonalWaypointCache personalWaypointCache() {
    return personalWaypointCache;
  }

  public PublicWaypointCache publicWaypointCache() {
    return publicWaypointCache;
  }

}
