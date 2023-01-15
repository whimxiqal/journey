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

package net.whimxiqal.journey.common.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.navigation.ModeType;
import net.whimxiqal.journey.common.navigation.Path;
import net.whimxiqal.journey.common.navigation.Step;
import net.whimxiqal.journey.common.search.PathTrial;
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
        trial.getOrigin().getX(),
        trial.getOrigin().getY(),
        trial.getOrigin().getZ(),
        trial.getDestination().getX(),
        trial.getDestination().getY(),
        trial.getDestination().getZ(),
        trial.getDomain(),
        trial.getCostFunction().getType(),
        cells,
        modes
    );
    pathTrialRecordId++;
    pathTrialRecords.add(record);
    ArrayList<Step> steps = trial.getPath().getSteps();
    for (int i = 0; i < steps.size(); i++) {
      PathTrialCellRecord cellRecord = new PathTrialCellRecord(
          record,
          steps.get(i).location().getX(),
          steps.get(i).location().getY(),
          steps.get(i).location().getZ(),
          i,
          steps.get(i).modeType());
      pathTrialCellRecords.add(cellRecord);
      cells.add(cellRecord);
    }
    modeTypes.forEach(type -> modes.add(new PathTrialModeRecord(record, type)));
  }

  @Override
  public void clear() {
    pathTrialRecords.clear();
    pathTrialCellRecords.clear();
  }

  @Override
  public @NotNull List<PathTrialRecord> getRecords(Cell origin, Cell destination) {
    return pathTrialRecords;
  }

  @Override
  public @Nullable PathTrialRecord getRecord(Cell origin, Cell destination, Set<ModeType> modeTypes) {
    if (!origin.domainId().equals(destination.domainId())) {
      return null;
    }
    for (PathTrialRecord record : pathTrialRecords) {
      boolean sameLocations = record.originX() == origin.getX()
          && record.originY() == origin.getY()
          && record.originZ() == origin.getZ()
          && record.worldId().equals(origin.domainId())
          && record.destinationX() == destination.getX()
          && record.destinationY() == destination.getY()
          && record.destinationZ() == destination.getZ();
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
    return new Path(new Cell(record.originX(), record.originY(), record.originZ(), record.worldId()),
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
