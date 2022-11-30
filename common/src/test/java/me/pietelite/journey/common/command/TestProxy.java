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

package me.pietelite.journey.common.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import me.pietelite.mantle.common.Logger;
import me.pietelite.mantle.common.Proxy;

public class TestProxy implements Proxy {

  private final TestLogger logger = new TestLogger();
  private final Map<UUID, Set<String>> permissions = new HashMap<>();

  @Override
  public Logger logger() {
    return logger;
  }

  @Override
  public boolean hasPermission(UUID uuid, String s) throws NoSuchElementException {
    return false;
  }

  public void setPermission(UUID uuid, String perm) {
    permissions.computeIfAbsent(uuid, k -> new HashSet<>()).add(perm);
  }

  public void unsetPermission(UUID uuid, String perm) {
    Set<String> perms = permissions.get(uuid);
    if (perms != null) {
      perms.remove(perm);
    }
  }

  @Override
  public List<String> onlinePlayerNames() {
    return Collections.emptyList();
  }

  @Override
  public List<String> worldNames() {
    return Collections.emptyList();
  }
}
