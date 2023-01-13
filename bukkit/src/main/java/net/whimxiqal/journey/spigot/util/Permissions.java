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

package net.whimxiqal.journey.spigot.util;

import org.bukkit.permissions.Permission;

/**
 * Utility class to enumerate {@link Permission}s.
 */
public final class Permissions {

  public static final Permission ADMIN = new Permission("journey.admin");
  public static final Permission JOURNEY_PATH_PLAYER = new Permission("journey.path.player");
  public static final Permission JOURNEY_PATH_DEATH = new Permission("journey.path.death");
  public static final Permission JOURNEY_PATH_PRIVATE = new Permission("journey.path.private");
  public static final Permission JOURNEY_PATH_SURFACE = new Permission("journey.path.surface");
  public static final Permission JOURNEY_PATH_QUEST = new Permission("journey.path.quest");
  public static final Permission JOURNEY_PATH_PUBLIC = new Permission("journey.path.public");
  public static final Permission JOURNEY_EDIT_PUBLIC = new Permission("journey.edit.public");

  private Permissions() {
  }

}
