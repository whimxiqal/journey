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

package net.whimxiqal.journey.data;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.Value;
import lombok.experimental.Accessors;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.navigation.Path;
import net.whimxiqal.journey.search.PathTrial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A manager to record paths during searches.
 */
public interface PathRecordManager {

  /**
   * Record a path trial.
   *
   * @param trial            the trial
   * @param modeTypes        group of modes used to calculate this path trial
   * @param executionTime    the time it took to execute
   * @throws DataAccessException when data is accessed incorrectly
   */
  void report(PathTrial trial,
              Set<ModeType> modeTypes,
              long executionTime) throws DataAccessException;

  /**
   * Clear all records. <b>Dangerous!</b>
   */
  void truncate();

  int totalRecordCellCount();

  /**
   * Ger any records matching a start and end.
   *
   * @param origin      the original cell
   * @param destination the destination cell
   * @return any records
   */
  @NotNull
  List<PathTrialRecord> getRecords(Cell origin, Cell destination);

  /**
   * Get a specific record, if it exists.
   *
   * @param origin      the original cell
   * @param destination the destination cell
   * @param modeTypes   the mode types used to traverse to the destination
   * @return the record, if it exists
   */
  @Nullable
  PathTrialRecord getRecord(Cell origin, Cell destination, Set<ModeType> modeTypes);

  /**
   * Get a specific path, using a constructor for cells.
   *
   * @param origin      the original cell
   * @param destination the destination cell
   * @param modeTypes   the mode types used to traverse to the destination
   * @return the new path
   */
  Path getPath(Cell origin, Cell destination, Set<ModeType> modeTypes);

  /**
   * Return whether a record exists with the given criteria.
   *
   * @param origin      the original cell
   * @param destination the destination cell
   * @param modeTypes   the mode types used to traverse to the destination
   * @return whether it exists
   */
  boolean containsRecord(Cell origin, Cell destination, Set<ModeType> modeTypes);

  /**
   * A record that represents a saved {@link PathTrial}.
   */
  @Value
  @Accessors(fluent = true)
  @SuppressWarnings("checkstyle:MethodName")
  class PathTrialRecord {
    long id;
    Date date;
    long duration;
    double pathCost;
    int originX;
    int originY;
    int originZ;
    int destinationX;
    int destinationY;
    int destinationZ;
    int domain;
    List<PathTrialCellRecord> cells;
    Collection<PathTrialModeRecord> modes;
  }

  /**
   * A record that represents a saved {@link Cell} within a {@link PathTrial}.
   */
  @Value
  @Accessors(fluent = true)
  @SuppressWarnings("checkstyle:MemberName")
  class PathTrialCellRecord {
    PathTrialRecord record;
    int x;
    int y;
    int z;
    Integer index;
    ModeType modeType;

    public Cell toCell() {
      return new Cell(x, y, z, record.domain);
    }
  }

  /**
   * A record to store a {@link Mode}.
   */
  @Value
  @Accessors(fluent = true)
  class PathTrialModeRecord {
    PathTrialRecord record;
    ModeType modeType;
  }

}
