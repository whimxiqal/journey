package edu.whimc.indicator.common.search.tracker;

import edu.whimc.indicator.common.path.Cell;
import edu.whimc.indicator.common.path.ModeType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class SearchAnimator<T extends Cell<T, D>, D> implements SearchTracker<T, D> {

  private final int delayMillis;
  boolean valid = true;

  @Override
  public final void acceptResult(T cell, SearchTracker.Result result, ModeType modeType) {
    if (!valid) {
      return;
    }

    if (showResult(cell, result)) {
      try {
        Thread.sleep(delayMillis);
      } catch (InterruptedException e) {
        e.printStackTrace();
        valid = false;
      }
    }
  }

  protected abstract boolean showResult(T cell, SearchTracker.Result result);

}
