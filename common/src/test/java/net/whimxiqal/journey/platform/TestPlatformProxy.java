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

package net.whimxiqal.journey.platform;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.whimxiqal.journey.common.math.Vector;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.navigation.ModeType;
import net.whimxiqal.journey.common.navigation.PlatformProxy;
import net.whimxiqal.journey.common.search.AnimationManager;
import net.whimxiqal.journey.common.search.SearchSession;
import net.whimxiqal.journey.common.search.flag.FlagSet;

public class TestPlatformProxy implements PlatformProxy {

  public static Map<String, TestWorld> worlds = new HashMap<>();
  public static Map<String, Cell> pois = new HashMap<>();
  public static List<TestPort> ports = new LinkedList<>();

  @Override
  public boolean isNetherPortal(Cell cell) {
    return false;
  }

  @Override
  public void playSuccess(UUID playerUuid) {
    // ignore
  }

  @Override
  public void spawnDestinationParticle(UUID playerUuid, String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
    // ignore
  }

  @Override
  public void spawnModeParticle(UUID playerUuid, ModeType type, String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
    // ignore
  }

  @Override
  public Optional<UUID> onlinePlayer(String name) {
    return Optional.empty();
  }

  @Override
  public Optional<Cell> entityCellLocation(UUID entityUuid) {
    return Optional.of(new Cell(0, 0, 0, WorldLoader.worldResources[0]));  // just say everything is at the origin
  }

  @Override
  public Optional<Vector> entityVector(UUID entityUuid) {
    return Optional.empty();
  }

  @Override
  public void prepareSearchSession(SearchSession searchSession, UUID player, FlagSet flags, boolean includePorts) {
    searchSession.registerMode(new WalkMode(searchSession));
    ports.forEach(searchSession::registerPort);
  }

  @Override
  public void prepareDestinationSearchSession(SearchSession searchSession, UUID player, FlagSet flags, Cell destination) {
    // do nothing extra here
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
