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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.whimxiqal.journey.integration.citizens.config.ConfigSettings;
import org.bukkit.entity.EntityType;

public class GuideStore {

  private final Set<Integer> busyGuideNpcs = new HashSet<>();
  private final LinkedList<Integer> idleGuideNpcs = new LinkedList<>();
  private boolean enabled = false;

  public void initialize() throws IOException {
    final int maxGuides = ConfigSettings.NPC_NAVIGATOR_MAX_NPCS.load();
    for (NPC npc : CitizensAPI.getNPCRegistry()) {
      if (!npc.hasTrait(GuideTrait.class)) {
        continue;
      }

      if (idleGuideNpcs.size() >= maxGuides) {
        // We already have the max number of guides allowed, so get rid of this one
        npc.destroy();
      } else {
        idleGuideNpcs.add(npc.getId());
        if (npc.isSpawned()) {
          // despawn because there's no active navigations so we shouldn't have it anyway
          npc.despawn(DespawnReason.PLUGIN);
        }
      }
    }
    JourneyCitizens.logger().info("Found " + idleGuideNpcs.size() + " guide NPCs");
    enabled = true;
  }

  public void shutdown() {
    enabled = false;
    for (int id : busyGuideNpcs) {
      NPC npc = CitizensAPI.getNPCRegistry().getById(id);
      if (npc == null) {
        continue;
      }
      if (npc.isSpawned()) {
        npc.despawn();
      }
    }
    busyGuideNpcs.clear();
  }

  /**
   * Reserve a new guide
   *
   * @param entityType the entity type
   * @param name       the entity's name
   * @return the npc, or null if none could be created due to limitations
   * @throws IOException if an error occurred trying to store the reservation
   */
  NPC issueGuide(EntityType entityType, String name) throws IOException {
    if (!enabled) {
      return null;
    }
    while (!idleGuideNpcs.isEmpty()) {
      Integer id = idleGuideNpcs.pop();
      NPC npc = CitizensAPI.getNPCRegistry().getById(id);
      if (npc == null) {
        continue;
      }
      if (!npc.hasTrait(GuideTrait.class)) {
        // not a guide anymore
        continue;
      }
      busyGuideNpcs.add(id);
      npc.setBukkitEntityType(entityType);
      npc.setName(name);
      npc.setFlyable(true);
      npc.setUseMinecraftAI(true);
      return npc;
    }
    // there are no idle guides
    if (busyGuideNpcs.size() < ConfigSettings.NPC_NAVIGATOR_MAX_NPCS.load()) {
      // we can create a new one
      NPC npc = CitizensAPI.getNPCRegistry().createNPC(entityType, name);
      npc.addTrait(GuideTrait.class);
      busyGuideNpcs.add(npc.getId());
      npc.setFlyable(true);
      npc.setUseMinecraftAI(true);
      return npc;
    }
    return null;
  }

  void releaseGuide(int id) {
    if (!busyGuideNpcs.contains(id)) {
      return;
    }
    busyGuideNpcs.remove(id);
    idleGuideNpcs.add(id);
  }

}
