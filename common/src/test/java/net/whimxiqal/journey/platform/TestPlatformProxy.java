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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.math.Vector;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.navigation.PlatformProxy;
import net.whimxiqal.journey.search.AnimationManager;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.flag.FlagSet;
import org.bstats.charts.CustomChart;

public class TestPlatformProxy implements PlatformProxy {

  public static Map<Integer, TestWorld> worlds = new HashMap<>();  // domain -> world
  public static Map<String, Cell> pois = new HashMap<>();
  public static List<Tunnel> tunnels = new LinkedList<>();
  public static List<JourneyPlayer> onlinePlayers = new LinkedList<>();

  @Override
  public boolean isNetherPortal(Cell cell) {
    return false;
  }

  @Override
  public void playSuccess(UUID playerUuid) {
    // ignore
  }

  @Override
  public void spawnDestinationParticle(UUID playerUuid, int domain, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
    // ignore
  }

  @Override
  public void spawnModeParticle(UUID playerUuid, ModeType type, int domain, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
    // ignore
  }

  @Override
  public Collection<JourneyPlayer> onlinePlayers() {
    return onlinePlayers;
  }

  @Override
  public Optional<JourneyPlayer> onlinePlayer(UUID uuid) {
    return onlinePlayers.stream().filter(player -> player.uuid().equals(uuid)).findFirst();
  }

  @Override
  public Optional<JourneyPlayer> onlinePlayer(String name) {
    return onlinePlayers.stream().filter(player -> player.name().equals(name)).findFirst();
  }

  @Override
  public Optional<Cell> entityCellLocation(UUID entityUuid) {
    return Optional.of(new Cell(0, 0, 0, WorldLoader.domain(0)));  // just say everything is at the origin
  }

  @Override
  public Optional<Vector> entityVector(UUID entityUuid) {
    return Optional.empty();
  }

  @Override
  public void prepareSearchSession(SearchSession searchSession, UUID player, FlagSet flags, boolean includePorts) {
    searchSession.registerMode(new WalkMode(searchSession));
    tunnels.forEach(searchSession::registerTunnel);
  }

  @Override
  public void prepareDestinationSearchSession(SearchSession searchSession, UUID player, FlagSet flags, Cell destination) {
    // do nothing extra here
  }

  @Override
  public boolean isAtSurface(Cell cell) {
    return false;
  }

  @Override
  public boolean sendBlockData(UUID player, Cell location, AnimationManager.StageType stage, ModeType mode) {
    return false;
  }

  @Override
  public boolean resetBlockData(UUID player, Collection<Cell> locations) {
    return false;
  }

  @Override
  public String domainName(int domain) {
    return worlds.get(domain).name;
  }

  @Override
  public boolean sendGui(JourneyPlayer player) {
    return false;
  }

  @Override
  public boolean synchronous() {
    return true;
  }

  @Override
  public Consumer<CustomChart> bStatsChartConsumer() {
    return chart -> {/* nothing */};
  }

  @Override
  public Map<String, Map<String, Integer>> domainResourceKeys() {
    return Collections.singletonMap("whimxiqal", TestPlatformProxy.worlds.values().stream().collect(Collectors.toMap(k -> k.name, k -> Journey.get().domainManager().domainIndex(k.uuid))));
  }
}
