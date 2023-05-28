package net.whimxiqal.journey.proxy;

import net.whimxiqal.journey.chunk.Directional;

/**
 * A Journey representation of a Minecraft door.
 */
public interface JourneyDoor extends Directional {

  boolean isOpen();

  boolean isIron();

}
