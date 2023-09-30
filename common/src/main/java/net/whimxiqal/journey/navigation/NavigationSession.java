package net.whimxiqal.journey.navigation;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.math.Vector;
import net.whimxiqal.journey.navigation.option.NavigatorOptionValues;
import net.whimxiqal.journey.search.SearchStep;

public class NavigationSession implements NavigationProgress {

  private static final int DISTANCE_REQUIRED_TO_COMPLETE_SQUARED = 4;
  private static final int NAVIGATION_LOOKAHEAD = 64;
  private final JourneyAgent agent;
  private final List<? extends SearchStep> steps;
  private final NavigatorOptionValues optionValues;
  private final CompletableFuture<NavigationResult> resultFuture;
  private NavigationStep currentNavigationStep;
  private int currentStepIndex = 0;
  private double currentStepProgress = 0;

  public NavigationSession(JourneyAgent agent, List<? extends SearchStep> steps, NavigatorOptionValues optionValues) {
    this.agent = agent;
    if (steps.isEmpty()) {
      throw new IllegalArgumentException("Steps may not be empty");
    }
    this.steps = steps;
    this.optionValues = optionValues;
    this.resultFuture = new CompletableFuture<>();
  }

  private void finish() {
    Component message = optionValues.value(NavigationManager.COMPLETION_MESSAGE_OPTION);
    if (Component.IS_NOT_EMPTY.test(message)) {
      agent.audience().sendMessage(message);
    }
    Component title = optionValues.value(NavigationManager.COMPLETION_TITLE_OPTION);
    Component subtitle = optionValues.value(NavigationManager.COMPLETION_SUBTITLE_OPTION);
    if (Component.IS_NOT_EMPTY.test(title) || Component.IS_NOT_EMPTY.test(subtitle)) {
      agent.audience().showTitle(Title.title(title, subtitle));
    }
    resultFuture.complete(NavigationResult.COMPLETED);
  }

  public boolean visit(Cell location) {
    if (destination().distanceToSquared(location) < DISTANCE_REQUIRED_TO_COMPLETE_SQUARED) {
      // We have reached our destination for the given path
      finish();
      return true;
    }

    int originalStepIndex = currentStepIndex;
    double originalStepProgress = currentStepProgress;
    Vector vector = new Vector(location.blockX(), location.blockY(), location.blockZ());
    do {
      if (currentNavigationStep == null) {
        currentNavigationStep = new NavigationStep(steps.get(Math.max(0, currentStepIndex - 1)).location(),
            steps.get(currentStepIndex).location());
      }

      // Check progress along current step
      double currentCompletedStepLength = currentStepProgress * currentNavigationStep.length();
      Vector locRelative = vector.subtract(currentNavigationStep.startVector());  // location of entity relative to start of step
      double completedLength = Math.max(currentCompletedStepLength, locRelative.projectionOnto(currentNavigationStep.path()));

      if (completedLength >= currentNavigationStep.length()) {
        completedLength = currentNavigationStep.length();
      }
      currentStepProgress = completedLength / currentNavigationStep.length();

      if (currentStepProgress < 1) {
        // Not done traversing current step
        break;
      }
      // done with navigation step
      if (currentStepIndex >= steps.size() - 1) {
        // done with all steps, wait for them to visit the actual destination
        return false;
      }
      // move on to next step
      int previousStepIndex = currentStepIndex;
      currentStepIndex++;
      currentStepProgress = 0;
      currentNavigationStep = new NavigationStep(steps.get(previousStepIndex).location(),
          steps.get(currentStepIndex).location());
      steps.get(currentStepIndex).prompt();
    } while (true);

    if (originalStepIndex == currentStepIndex && originalStepProgress == currentStepProgress) {
      // we haven't made any progress this step, so let's try the more calculation-intensive
      // block-by-block search to see if the player has walked into a future location
      for (int i = currentStepIndex; i < Math.min(steps.size(), currentStepIndex + NAVIGATION_LOOKAHEAD); i++) {
        SearchStep step = steps.get(currentStepIndex);
        if (!location.equals(step.location())) {
          continue;
        }
        currentStepIndex = i;
        currentStepProgress = 0;
        currentNavigationStep = null;
        step.prompt();
        break;
      }
    }
    return false;
  }

  @Override
  public List<? extends SearchStep> steps() {
    return steps;
  }

  @Override
  public int currentStepIndex() {
    return currentStepIndex;
  }

  @Override
  public double currentStepProgress() {
    return currentStepProgress;
  }

  public Cell destination() {
    return steps.get(steps.size() - 1).location();
  }

  public CompletableFuture<NavigationResult> resultFuture() {
    return resultFuture;
  }

}
