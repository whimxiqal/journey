package edu.whimc.indicator.spigot.path;

import edu.whimc.indicator.api.path.Link;
import org.bukkit.World;

public final class LinkImpl implements Link<CellImpl, World> {

    private final CellImpl origin;
    private final CellImpl destination;

    public LinkImpl(final CellImpl origin, final CellImpl destination) {
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public CellImpl getOrigin() {
        return origin;
    }

    @Override
    public CellImpl getDestination() {
        return destination;
    }
}
