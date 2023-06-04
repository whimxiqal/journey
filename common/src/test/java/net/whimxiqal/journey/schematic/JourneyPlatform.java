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

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Registries;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.enginehub.piston.CommandManager;
import org.jetbrains.annotations.Nullable;

public class JourneyPlatform extends AbstractPlatform {

  @Override
  public Registries getRegistries() {
    return null;
  }

  @Override
  public int getDataVersion() {
    return 0;
  }

  @Override
  public boolean isValidMobType(String type) {
    return false;
  }

  @Nullable
  @Override
  public Player matchPlayer(Player player) {
    return null;
  }

  @Nullable
  @Override
  public World matchWorld(World world) {
    return null;
  }

  @Override
  public void registerCommands(CommandManager commandManager) {

  }

  @Override
  public void setGameHooksEnabled(boolean enabled) {

  }

  @Override
  public LocalConfiguration getConfiguration() {
    return new LocalConfiguration() {
      @Override
      public void load() {
        // do nothing
      }
    };
  }

  @Override
  public String getVersion() {
    return null;
  }

  @Override
  public String getPlatformName() {
    return "Journey Platform";
  }

  @Override
  public String getPlatformVersion() {
    return "0";
  }

  @Override
  public Map<Capability, Preference> getCapabilities() {
    Map<Capability, Preference> out = new HashMap<>();
    out.put(Capability.CONFIGURATION, Preference.PREFERRED);
    out.put(Capability.WORLD_EDITING, Preference.PREFERRED);
    return out;
  }

  @Override
  public Set<SideEffect> getSupportedSideEffects() {
    return null;
  }
}
