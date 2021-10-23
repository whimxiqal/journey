package edu.whimc.indicator.spigot.search;

public class SessionState {

  /**
   * True if the algorithm has found a path.
   */
  private boolean solved = false;
  private int successNotificationTaskId = 0;
  /**
   * True if the player has been presented with a solution (itinerary -> journey).
   */
  private boolean solutionPresented = false;

  public void setSolved(boolean solved) {
    this.solved = solved;
  }

  public boolean wasSolved() {
    return solved;
  }

  public void setSuccessNotificationTaskId(int successNotificationTaskId) {
    this.successNotificationTaskId = successNotificationTaskId;
  }

  public int getSuccessNotificationTaskId() {
    return successNotificationTaskId;
  }

  public void setSolutionPresented(boolean solutionPresented) {
    this.solutionPresented = solutionPresented;
  }

  public boolean wasSolutionPresented() {
    return solutionPresented;
  }

}
