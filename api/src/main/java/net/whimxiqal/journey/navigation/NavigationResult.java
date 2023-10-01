package net.whimxiqal.journey.navigation;

/**
 * The final result of navigation.
 */
public enum NavigationResult {

  /**
   * The agent reached their destination during the lifetime of the navigator.
   */
  COMPLETED,

  /**
   * The navigator failed to start.
   */
  FAILED_START,

  /**
   * The navigator failed while running.
   */
  FAILED_RUNNING,

  /**
   * The navigator failed because it was shutdown while running.
   */
  FAILED_SHUTDOWN

}
