package edu.whimc.indicator.path;

import edu.whimc.indicator.api.path.Link;
import org.bukkit.World;

public final class SpigotLink implements Link<SpigotLocatable, World> {

    private final SpigotLocatable origin;
    private final SpigotLocatable destination;

    public SpigotLink(final SpigotLocatable origin, final SpigotLocatable destination) {
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public SpigotLocatable getOrigin() {
        return origin;
    }

    @Override
    public SpigotLocatable getDestination() {
        return destination;
    }
}
