package edu.whimc.journey.spigot.search;

/**
 * An object to help hold the current state of a running
 * {@link edu.whimc.journey.common.search.SearchSession}.
 */
public class PlayerSessionState {

  /**
   * True if the algorithm has found a path.
   */
  private boolean solved = false;
  private int successNotificationTaskId = 0;
  /**
   * True if the player has been presented with a solution (itinerary -> journey).
   */
  private boolean solutionPresented = false;

  /**
   * Set whether the search has been solved already or not.
   *
   * @param solved true if it's solved
   */
  public void setSolved(boolean solved) {
    this.solved = solved;
  }

  /**
   * Get whether the session was already solved.
   *
   * @return true if solved
   */
  public boolean wasSolved() {
    return solved;
  }

  /**
   * Get the success notification task id for the purpose
   * of possibly using it to canceling the task.
   *
   * @return the task id
   */
  public int getSuccessNotificationTaskId() {
    return successNotificationTaskId;
  }

  /**
   * Set the success notification task id for the purpose
   * of possibly canceling the task at a later time.
   *
   * @param successNotificationTaskId the id
   */
  public void setSuccessNotificationTaskId(int successNotificationTaskId) {
    this.successNotificationTaskId = successNotificationTaskId;
  }

  /**
   * Set whether a solution has been presented to the player already.
   *
   * @param solutionPresented true if presented, false if not yet presented
   */
  public void setSolutionPresented(boolean solutionPresented) {
    this.solutionPresented = solutionPresented;
  }

  /**
   * Get whether a solution has been presented to the player already.
   *
   * @return true if presented, false if not yet presented
   */
  public boolean wasSolutionPresented() {
    return solutionPresented;
  }

}
