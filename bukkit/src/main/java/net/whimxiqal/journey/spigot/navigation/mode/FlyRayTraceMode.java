package net.whimxiqal.journey.spigot.navigation.mode;

import java.util.List;
import java.util.Set;
import net.whimxiqal.journey.common.Journey;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.navigation.ModeType;
import net.whimxiqal.journey.common.search.SearchSession;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class FlyRayTraceMode extends RayTraceMode {
  public FlyRayTraceMode(SearchSession session, Set<Material> forcePassable, Cell destination) {
    super(session, forcePassable, destination, 0.6, 1.8, 0.6, FluidCollisionMode.NEVER);
  }

  @Override
  public @NotNull ModeType type() {
    return ModeType.FLY;
  }

  @Override
  protected boolean check(Cell origin) {
    return Journey.get().proxy().platform().isAtSurface(origin);
  }

  protected void finish(Cell origin, Cell destination, List<Option> options) {
    options.add(Option.between(origin, destination, 1));
  }
}
