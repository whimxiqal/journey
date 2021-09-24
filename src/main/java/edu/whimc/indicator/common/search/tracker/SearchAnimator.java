package edu.whimc.indicator.common.search.tracker;

import edu.whimc.indicator.common.navigation.Cell;
import edu.whimc.indicator.common.navigation.ModeType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class SearchAnimator<T extends Cell<T, D>, D> extends BlankSearchTracker<T, D> {

  private final int delayMillis;
  boolean animating = true;

  @Override
  public final void acceptResult(T cell, SearchTracker.Result result, ModeType modeType) {
    if (!animating) {
      return;
    }

    if (showResult(cell, result, modeType)) {
      try {
        Thread.sleep(delayMillis);
      } catch (InterruptedException e) {
        e.printStackTrace();
        animating = false;
      }
    }
  }

  protected final void setAnimating(boolean value) {
    this.animating = value;
  }

  protected abstract boolean showResult(T cell, SearchTracker.Result result, ModeType modeType);

}
