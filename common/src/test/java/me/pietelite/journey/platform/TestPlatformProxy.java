/*
 * MIT License
 *
 * Copyright 2022 Pieter Svenson
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
 *
 */

package me.pietelite.journey.platform;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.navigation.PlatformProxy;
import me.pietelite.journey.common.search.AnimationManager;
import me.pietelite.journey.common.search.SearchSession;
import me.pietelite.journey.common.search.flag.FlagSet;

public class TestPlatformProxy implements PlatformProxy {

  public static Map<String, TestWorld> worlds = new HashMap<>();
  public static List<TestPort> ports = new LinkedList<>();

  @Override
  public boolean isNetherPortal(Cell cell) {
    return false;
  }

  @Override
  public void playSuccess(UUID playerUuid) {

  }

  @Override
  public void spawnDestinationParticle(String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {

  }

  @Override
  public void spawnModeParticle(ModeType type, String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {

  }

  @Override
  public Optional<UUID> onlinePlayer(String name) {
    return Optional.empty();
  }

  @Override
  public Cell entityLocation(UUID entityUuid) {
    return null;
  }

  @Override
  public void prepareSearchSession(SearchSession searchSession, UUID player, FlagSet flags, boolean includePorts) {
    searchSession.registerMode(new WalkMode());
    ports.forEach(searchSession::registerPort);
  }

  @Override
  public boolean isAtSurface(Cell cell) {
    return true;
  }

  @Override
  public boolean sendBlockData(UUID player, Cell location, AnimationManager.StageType stage, ModeType mode) {
    return false;
  }

  @Override
  public boolean resetBlockData(UUID player, Collection<Cell> locations) {
    return false;
  }
}
