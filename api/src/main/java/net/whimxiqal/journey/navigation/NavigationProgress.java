package net.whimxiqal.journey.navigation;

import java.util.List;
import net.whimxiqal.journey.search.SearchStep;

public interface NavigationProgress {

  List<? extends SearchStep> steps();

  int currentStepIndex();

  double currentStepProgress();

}
