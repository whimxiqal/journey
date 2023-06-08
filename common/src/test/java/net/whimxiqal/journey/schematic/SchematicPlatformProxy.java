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

package net.whimxiqal.journey.schematic;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.InternalJourneyPlayer;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.chunk.ChunkId;
import net.whimxiqal.journey.math.Vector;
import net.whimxiqal.journey.search.ModeType;
import net.whimxiqal.journey.navigation.PlatformProxy;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyChunk;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.flag.FlagSet;
import org.bstats.charts.CustomChart;

public class SchematicPlatformProxy implements PlatformProxy {

  private final Supplier<Clipboard> clipboard;

  SchematicPlatformProxy(Supplier<Clipboard> clipboardSupplier) {
    this.clipboard = clipboardSupplier;
  }

  @Override
  public JourneyChunk toChunk(ChunkId chunkId) {
    return new SchematicChunk(chunkId, clipboard.get());
  }

  @Override
  public JourneyBlock toBlock(Cell cell) {
    return new SchematicBlock(cell, clipboard.get().getBlock(BlockVector3.at(cell.blockX(), cell.blockY(), cell.blockZ())).getBlockType());
  }

  @Override
  public void playSuccess(UUID playerUuid) {
    // do nothing
  }

  @Override
  public void spawnDestinationParticle(UUID playerUuid, int domain, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
    // do nothing
  }

  @Override
  public void spawnModeParticle(UUID playerUuid, ModeType type, int domain, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
    // do nothing
  }

  @Override
  public Collection<InternalJourneyPlayer> onlinePlayers() {
    return Collections.singletonList(SchematicSearchTests.PLAYER);
  }

  @Override
  public Optional<InternalJourneyPlayer> onlinePlayer(UUID uuid) {
    if (uuid.equals(SchematicSearchTests.PLAYER.uuid())) {
      return Optional.of(SchematicSearchTests.PLAYER);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<InternalJourneyPlayer> onlinePlayer(String name) {
    return Optional.empty();
  }

  @Override
  public Optional<Cell> entityCellLocation(UUID entityUuid) {
    return Optional.empty();
  }

  @Override
  public Optional<Vector> entityVector(UUID entityUuid) {
    return Optional.empty();
  }

  @Override
  public void prepareDestinationSearchSession(SearchSession searchSession, JourneyAgent agent, FlagSet flags, Cell destination) {

  }

  @Override
  public void sendAnimationBlock(UUID player, Cell location) {

  }

  @Override
  public void resetAnimationBlocks(UUID player, Collection<Cell> locations) {

  }

  @Override
  public String domainName(int domainId) {
    return null;
  }

  @Override
  public boolean sendGui(JourneyPlayer source) {
    return false;
  }

  @Override
  public Consumer<CustomChart> bStatsChartConsumer() {
    return (chart) -> {};
  }

  @Override
  public Map<String, Map<String, Integer>> domainResourceKeys() {
    return Collections.emptyMap();
  }
}
