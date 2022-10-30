/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package me.pietelite.journey.spigot.navigation.mode;

import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.search.SearchSession;
import java.util.List;
import java.util.Set;
import me.pietelite.journey.spigot.util.SpigotUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * A mode to provide the results to whether a player can climb blocks around them,
 * like ladders or vines.
 */
public final class BoatMode extends SpigotMode {

    public final double DISTANCE_MULTIPLIER = 5.6 / 8;

    /**
     * General constructor.
     *
     * @param forcePassable a set of materials deemed passable
     */
    public BoatMode(SearchSession session, Set<Material> forcePassable) {
        super(session, forcePassable);
    }

    @Override
    protected void collectDestinations(@NotNull Cell origin, @NotNull List<Option> options) {
        Cell cell;
        for (int offX = -1; offX <= 1; offX++) {
            outerZ:
            for (int offZ = -1; offZ <= 1; offZ++) {
                // For the diagonal points, check that the path is clear in both
                //  lateral directions and diagonally
                for (int insideOffX = offX * offX /* normalize sign */; insideOffX >= 0; insideOffX--) {
                    for (int insideOffZ = offZ * offZ /* normalize sign */; insideOffZ >= 0; insideOffZ--) {
                        if (insideOffX == 0 && insideOffZ == 0) {
                            continue;
                        }
                        for (int offY = 0; offY <= 1; offY++) { // Check two blocks tall
                            cell = origin.atOffset(insideOffX * offX /* get sign back */,
                                    offY - 1,
                                    insideOffZ * offZ /*get sign back */);
                            if (!isLaterallyPassable(SpigotUtil.getBlock(cell))) {
                                reject(cell);
                                continue outerZ;  // Barrier - invalid move
                            }
                        }
                    }
                }

                // We can move to offX and offY laterally
                cell = origin.atOffset(offX, 0, offZ);
                if (SpigotUtil.getBlock(cell.atOffset(0, -1, 0)).getMaterial() == Material.WATER) {
                    // We can boat on it
                    accept(cell, origin.distanceTo(cell) * DISTANCE_MULTIPLIER, options);
                } else {
                    reject(cell);
                }
            }
        }
    }

    @Override
    public @NotNull ModeType type() {
        return ModeType.BOAT;
    }
}
