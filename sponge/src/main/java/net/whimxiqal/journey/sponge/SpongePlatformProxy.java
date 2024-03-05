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

package net.whimxiqal.journey.sponge;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Color;
import net.whimxiqal.journey.InternalJourneyPlayer;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.chunk.ChunkId;
import net.whimxiqal.journey.navigation.PlatformProxy;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyChunk;
import net.whimxiqal.journey.proxy.UnavailableJourneyChunk;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.flag.FlagSet;
import net.whimxiqal.journey.sponge.chunk.SpongeSessionJourneyBlock;
import net.whimxiqal.journey.sponge.chunk.SpongeSessionJourneyChunk;
import net.whimxiqal.journey.sponge.gui.SpongeJourneyGui;
import net.whimxiqal.journey.sponge.util.SpongeUtil;
import org.bstats.charts.CustomChart;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.chunk.WorldChunk;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

public class SpongePlatformProxy implements PlatformProxy {

  private final Metrics metrics;

  public SpongePlatformProxy(Metrics metrics) {
    this.metrics = metrics;
  }

  @Override
  public CompletableFuture<JourneyChunk> toChunk(ChunkId chunkId, boolean generate) {
    CompletableFuture<JourneyChunk> future = new CompletableFuture<>();
    Sponge.server().scheduler().submit(Task.builder().plugin(JourneySponge.get().container()).execute(() -> {
      ServerWorld world = SpongeUtil.getWorld(chunkId.domain());
      Optional<WorldChunk> chunk = world.loadChunk(chunkId.x(), 0, chunkId.z(), generate);
      if (chunk.isEmpty() || chunk.get().isEmpty()) {
        future.complete(new UnavailableJourneyChunk(chunkId));
      } else {
        future.complete(new SpongeSessionJourneyChunk(chunkId, chunk.get()));
      }
    }).build());
    return future;
  }

  @Override
  public JourneyBlock toBlock(Cell cell) {
    return new SpongeSessionJourneyBlock(cell, SpongeUtil.getBlock(cell), SpongeUtil.getBlock(cell.atOffset(0, -1, 0)), new FlagSet());
  }

  @Override
  public void spawnParticle(UUID playerUuid, String particleName, Color color, int domain, double x, double y, double z) {
    ParticleType type;
    if (particleName.equals("redstone")) {
      // special case for backporting. Technically, all "redstone" should be "dust"
      type = ParticleTypes.DUST.get();
    } else {
      Optional<RegistryEntry<ParticleType>> registryEntry = ParticleTypes.registry().findEntry(ResourceKey.minecraft(particleName));
      if (registryEntry.isEmpty()) {
        return;
      }
      type = registryEntry.get().value();
    }
    Optional<ServerPlayer> player = Sponge.server().player(playerUuid);
    if (player.isEmpty()) {
      return;
    }
    ParticleEffect.Builder builder = ParticleEffect.builder().type(type);
    if (type.equals(ParticleTypes.DUST.get())) {
      builder.option(ParticleOptions.COLOR, org.spongepowered.api.util.Color.ofRgb(color.red(), color.green(), color.blue()));
    }
    player.get().spawnParticles(builder.build(), Vector3d.from(x, y, z));
  }

  @Override
  public List<InternalJourneyPlayer> onlinePlayers() {
    return Sponge.server().onlinePlayers().stream().map(SpongeJourneyPlayer::new).collect(Collectors.toList());
  }

  @Override
  public Optional<InternalJourneyPlayer> onlinePlayer(UUID uuid) {
    return Sponge.server().player(uuid).map(SpongeJourneyPlayer::new);
  }

  @Override
  public Optional<InternalJourneyPlayer> onlinePlayer(String name) {
    return Sponge.server().player(name).map(SpongeJourneyPlayer::new);
  }

  @Override
  public Optional<Cell> entityCellLocation(UUID entityUuid) {
    for (ServerWorld world : Sponge.server().worldManager().worlds()) {
      Optional<Entity> entity = world.entity(entityUuid);
      if (entity.isPresent()) {
        return entity.map(e -> SpongeUtil.toCell(e.serverLocation()));
      }
    }
    return Optional.empty();
  }

  @Override
  public void prepareDestinationSearchSession(SearchSession searchSession, JourneyAgent agent, FlagSet flags, Cell destination) {
    // no op
  }

  @Override
  public void sendAnimationBlock(UUID playerUuid, Cell location) {
    Optional<ServerPlayer> player = Sponge.server().player(playerUuid);
    if (player.isEmpty()) {
      return;
    }
    if (SpongeUtil.toCell(player.get().serverLocation()).equals(location)
        || SpongeUtil.toCell(player.get().serverLocation().add(0, 1, 0)).equals(location)) {
      return;
    }
    showBlock(player.get(), location, BlockTypes.WHITE_STAINED_GLASS.get().defaultState());
  }

  @Override
  public void resetAnimationBlocks(UUID playerUuid, Collection<Cell> locations) {
    Optional<ServerPlayer> player = Sponge.server().player(playerUuid);
    if (player.isEmpty()) {
      return;
    }
    for (Cell cell : locations) {
      showBlock(player.get(), cell, SpongeUtil.getBlock(cell));
    }
  }

  private void showBlock(Player player, Cell cell, BlockState blockData) {
    if (SpongeUtil.getWorld(cell).equals(player.serverLocation().world())
        && cell.distanceToSquared(SpongeUtil.toCell(player.serverLocation())) < 10000 /* 100 blocks away, ignore */) {
      player.sendBlockChange(cell.blockX(), cell.blockY(), cell.blockZ(), blockData);
    }
  }

  @Override
  public String domainName(int domain) {
    return SpongeUtil.getWorld(domain).properties().name();
  }

  @Override
  public boolean sendGui(JourneyPlayer player) {
    SpongeJourneyGui gui = new SpongeJourneyGui(player);
    return gui.open();
  }

  @Override
  public void consumeChart(CustomChart chart) {
    metrics.addCustomChart(chart);
  }

  @Override
  public Map<String, Map<String, Integer>> domainResourceKeys() {
    Map<String, Map<String, Integer>> domains = new HashMap<>();
    for (ServerWorld world : Sponge.server().worldManager().worlds()) {
      ResourceKey key = world.properties().key();
      domains.computeIfAbsent(key.namespace(), k -> new HashMap<>()).put(key.value(), SpongeUtil.getDomain(world));
    }
    return domains;
  }

  @Override
  public List<String> particleTypes() {
    return ParticleTypes.registry().stream().map(particle -> particle.key(RegistryTypes.PARTICLE_TYPE).value()).collect(Collectors.toList());
  }

  @Override
  public boolean isValidParticleType(String particleType) {
    return ParticleTypes.registry().findEntry(ResourceKey.minecraft(particleType)).isPresent();
  }

}
