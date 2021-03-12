package edu.whimc.indicator.spigot.path;

import edu.whimc.indicator.api.path.Link;
import org.bukkit.World;

public final class LinkImpl implements Link<LocationCell, World> {

    private final LocationCell origin;
    private final LocationCell destination;

    public LinkImpl(final LocationCell origin, final LocationCell destination) {
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public LocationCell getOrigin() {
        return origin;
    }

    @Override
    public LocationCell getDestination() {
        return destination;
    }
}
