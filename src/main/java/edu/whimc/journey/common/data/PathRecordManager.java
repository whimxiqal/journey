/*
 * MIT License
 *
 * Copyright 2021 Pieter Svenson
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
 *
 */

package edu.whimc.journey.common.data;

import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.navigation.ModeType;
import edu.whimc.journey.common.navigation.ModeTypeGroup;
import edu.whimc.journey.common.navigation.Path;
import edu.whimc.journey.common.search.FlexiblePathTrial;
import edu.whimc.journey.common.search.PathTrial;
import edu.whimc.journey.common.search.ScoringFunction;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.jetbrains.annotations.NotNull;

public interface PathRecordManager<T extends Cell<T, D>, D> {

  void report(PathTrial<T, D> trial,
              Collection<FlexiblePathTrial.Node<T, D>> calculationNodes,
              ModeTypeGroup modeTypeGroup,
              long executionTime) throws DataAccessException;

  void clear();

  @NotNull
  List<PathTrialRecord> getRecords(T origin, T destination);

  PathTrialRecord getRecord(T origin, T destination, ModeTypeGroup modeTypes);

  Path<T, D> getPath(T origin, T destination, ModeTypeGroup modeTypeGroup,
                     Cell.CellConstructor<T, D> constructor);

  boolean containsRecord(T origin, T destination, ModeTypeGroup modeTypes);

  /**
   * Get all the cells. Dangerous if the database is large!
   *
   * @return all cells
   */
  @NotNull Collection<PathTrialCellRecord> getAllCells();

  @NotNull
  Collection<PathTrialCellRecord> getRandomTrainingCells(Random random, int count);

  boolean hasAtLeastCellCount(long count);

  @SuppressWarnings("checkstyle:MethodName")
  record PathTrialRecord(long id, Date date,
                         long duration,
                         double pathLength,
                         int originX, int originY, int originZ,
                         int destinationX, int destinationY, int destinationZ,
                         String worldId,
                         ScoringFunction.Type scoringFunctionType,
                         List<PathTrialCellRecord> cells,
                         Collection<PathTrialModeRecord> modes) {
  }

  @SuppressWarnings("checkstyle:MethodName")
  record PathTrialCellRecord(PathTrialRecord record,
                             int x, int y, int z,
                             boolean critical,
                             Integer index,
                             ModeType modeType,
                             double deviation,
                             double distance,
                             int distanceY,
                             int biome,
                             int dimension) {
  }

  record PathTrialModeRecord(PathTrialRecord record,
                             ModeType modeType) {
  }

}
