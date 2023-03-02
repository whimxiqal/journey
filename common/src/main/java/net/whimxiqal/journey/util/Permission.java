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

package net.whimxiqal.journey.util;

import java.util.LinkedList;
import java.util.List;

public enum Permission {

  CANCEL("journey.cancel"),

  // Waypoints/Paths (Journey-to)
  PATH_GUI("journey.path.gui"),
  PATH_PERSONAL("journey.path.personal"),
  PATH_SERVER("journey.path.server"),
  PATH_DEATH("journey.path.death"),
  PATH_SURFACE("journey.path.surface"),
  PATH_WORLD("journey.path.world"),
  PATH_PLAYER_ENTITY("journey.path.player.entity"),
  PATH_PLAYER_WAYPOINTS("journey.path.player.waypoints"),

  // Edit
  EDIT_PERSONAL("journey.edit.personal"),
  EDIT_PERSONAL_PUBLICITY("journey.edit.personal.publicity"),
  EDIT_SERVER("journey.edit.server"),

  // Admin
  ADMIN_DEBUG("journey.admin.debug"),
  ADMIN_CACHE("journey.admin.cache"),
  ADMIN_RELOAD("journey.admin.reload"),
  ADMIN_INFO("journey.admin.info"),

  // Flags
  FLAG_TIMEOUT("journey.flag.timeout"),
  FLAG_ANIMATE("journey.flag.animate"),
  FLAG_FLY("journey.flag.fly"),
  FLAG_DOOR("journey.flag.door"),
  FLAG_DIG("journey.flag.dig");

  public static final String JOURNEY_PATH_PERMISSION_PREFIX = "journey.path";
  private final String path;

  Permission(String path) {
    this.path = path;
  }

  public static List<String> journeyPathExtend(String perm) {
    List<String> list = new LinkedList<>();
    list.add(perm);
    if (!perm.startsWith(Permission.JOURNEY_PATH_PERMISSION_PREFIX)) {
      list.add(JOURNEY_PATH_PERMISSION_PREFIX + "." + perm);
    }
    return list;
  }

  public String path() {
    return path;
  }

}
