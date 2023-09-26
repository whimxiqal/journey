package net.whimxiqal.journey.navigation;

import java.util.List;
import net.whimxiqal.journey.Synchronous;
import net.whimxiqal.journey.search.SearchStep;

/**
 * A source of information about an {@link net.whimxiqal.journey.JourneyAgent}'s progress
 * while completing a {@link Navigator}.
 */
public interface NavigationProgress {

  /**
   * The steps in the path the agent must traverse.
   *
   * @return the steps
   */
  @Synchronous
  List<? extends SearchStep> steps();

  /**
   * The index in the list of steps of the path that corresponds to the {@link SearchStep}
   * that the agent is currently traversing.
   *
   * @return the index of the current step
   */
  @Synchronous
  int currentStepIndex();

  /**
   * The portion of the step that has been traversed so far, from [0, 1). So, if the
   * agent has traversed halfway from the destination of the previous step to the
   * destination of the current step, the progress would be 0.5.
   *
   * @return the progress made on the current step
   */
  @Synchronous
  double currentStepProgress();

}
