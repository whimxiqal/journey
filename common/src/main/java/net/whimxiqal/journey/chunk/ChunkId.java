package net.whimxiqal.journey.chunk;

/**
 * A simple identifier for a chunk, including a domain (world), x coordinate, and z coordinate.
 *
 * @param domain the domain id
 * @param x      the x coordinate
 * @param z      the z coordinate
 */
public record ChunkId(int domain, int x, int z) {
}
