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

package net.whimxiqal.journey.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.navigation.Path;
import net.whimxiqal.journey.navigation.Step;
import net.whimxiqal.journey.search.PathTrial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestPathRecordManager implements PathRecordManager {

  private final List<PathTrialRecord> pathTrialRecords = new LinkedList<>();
  private final List<PathTrialCellRecord> pathTrialCellRecords = new LinkedList<>();
  private long pathTrialRecordId = 0;

  @Override
  public void report(PathTrial trial, Set<ModeType> modeTypes, long executionTime) throws DataAccessException {
    List<PathTrialCellRecord> cells = new LinkedList<>();
    List<PathTrialModeRecord> modes = new LinkedList<>();
    PathTrialRecord record = new PathTrialRecord(
        pathTrialRecordId,
        new Date(),
        executionTime,
        trial.getLength(),
        trial.getOrigin().blockX(),
        trial.getOrigin().blockY(),
        trial.getOrigin().blockZ(),
        trial.getDestination().blockX(),
        trial.getDestination().blockY(),
        trial.getDestination().blockZ(),
        trial.getDomain(),
        cells,
        modes
    );
    pathTrialRecordId++;
    pathTrialRecords.add(record);
    ArrayList<Step> steps = trial.getPath().getSteps();
    for (int i = 0; i < steps.size(); i++) {
      PathTrialCellRecord cellRecord = new PathTrialCellRecord(
          record,
          steps.get(i).location().blockX(),
          steps.get(i).location().blockY(),
          steps.get(i).location().blockZ(),
          i,
          steps.get(i).modeType());
      pathTrialCellRecords.add(cellRecord);
      cells.add(cellRecord);
    }
    modeTypes.forEach(type -> modes.add(new PathTrialModeRecord(record, type)));
  }

  @Override
  public void truncate() {
    pathTrialRecords.clear();
    pathTrialCellRecords.clear();
  }

  @Override
  public int totalRecordCellCount() {
    return pathTrialCellRecords.size();
  }

  @Override
  public @NotNull List<PathTrialRecord> getRecords(Cell origin, Cell destination) {
    return pathTrialRecords;
  }

  @Override
  public @Nullable PathTrialRecord getRecord(Cell origin, Cell destination, Set<ModeType> modeTypes) {
    if (origin.domain() != destination.domain()) {
      return null;
    }
    for (PathTrialRecord record : pathTrialRecords) {
      boolean sameLocations = record.originX() == origin.blockX()
          && record.originY() == origin.blockY()
          && record.originZ() == origin.blockZ()
          && record.domain() == origin.domain()
          && record.destinationX() == destination.blockX()
          && record.destinationY() == destination.blockY()
          && record.destinationZ() == destination.blockZ();
      boolean modesSuperSet = true;
      for (PathTrialModeRecord mode : record.modes()) {
        if (!modeTypes.contains(mode.modeType())) {
          modesSuperSet = false;
          break;
        }
      }
      if (sameLocations && modesSuperSet) {
        return record;
      }
    }
    return null;
  }

  @Override
  public Path getPath(Cell origin, Cell destination, Set<ModeType> modeTypes) {
    PathTrialRecord record = getRecord(origin, destination, modeTypes);
    if (record == null) {
      return null;
    }
    LinkedList<Step> steps = new LinkedList<>();

    // Add the first one because we don't move to get here
    steps.add(new Step(record.cells().get(0).toCell(), 0, record.cells().get(0).modeType()));
    for (int i = 1; i < record.cells().size(); i++) {
      Cell cell = record.cells().get(i).toCell();
      steps.add(new Step(cell,
          cell.distanceTo(steps.getLast().location()),
          record.cells().get(i).modeType()));
    }
    return new Path(new Cell(record.originX(), record.originY(), record.originZ(), record.domain()),
        steps,
        record.pathCost());
  }

  @Override
  public boolean containsRecord(Cell origin, Cell destination, Set<ModeType> modeTypes) {
    return getRecord(origin, destination, modeTypes) != null;
  }

  @Override
  public @NotNull Collection<PathTrialCellRecord> getAllCells() {
    return pathTrialCellRecords;
  }
}
