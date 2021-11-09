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
import edu.whimc.journey.common.search.FlexiblePathTrial;
import edu.whimc.journey.common.search.PathTrial;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface PathReportManager<T extends Cell<T, D>, D> {

  void addReport(PathTrial<T, D> trial,
                 Collection<FlexiblePathTrial.Node<T, D>> calculationNodes,
                 long executionTime) throws DataAccessException;

  @Nullable
  PathTrialRecord getNextReport() throws DataAccessException;

  @SuppressWarnings("checkstyle:MethodName")
  record PathTrialRecord(long id, Date date,
                         int originX, int originY, int originZ,
                         int destinationX, int destinationY, int destinationZ,
                         UUID worldUuid,
                         Collection<PathTrialCellRecord> cells) {
  }

  @SuppressWarnings("checkstyle:MethodName")
  record PathTrialCellRecord(PathTrialRecord record,
                             int x, int y, int z,
                             int deviation,
                             int distance,
                             int distanceY,
                             int biome,
                             int dimension) {
  }

}
