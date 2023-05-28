/*
 * MIT License
 *
 * Copyright (c) whimxiqal
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

package net.whimxiqal.journey.search;

/**
 * An enumeration of possible result states from running processes.
 */
public enum ResultState {

  IDLE("idle"),
  RUNNING("running"),
  RUNNING_SUCCESSFUL("running (successful)"),
  STOPPING_CANCELED("stopping (canceled)"), // manually canceled
  STOPPING_FAILED("stopping (failed)"),
  STOPPING_SUCCESSFUL("stopping (successful)"),
  STOPPING_ERROR("stopping (error)"),
  STOPPED_FAILED("stopped (failed)"),
  STOPPED_SUCCESSFUL("stopped (successful)"),
  STOPPED_CANCELED("stopped (canceled)"),
  STOPPED_ERROR("stopped (error)");

  final String message;

  ResultState(String message) {
    this.message = message;
  }

  /**
   * Determine if the state is implying the process is still operating (running).
   *
   * @return true if running
   */
  public boolean isRunning() {
    switch (this) {
      case RUNNING:
      case RUNNING_SUCCESSFUL:
      case STOPPING_FAILED:
      case STOPPING_CANCELED:
      case STOPPING_SUCCESSFUL:
        return true;
      default:
        return false;
    }
  }

  /**
   * Determine if this state is implying the process has finished operating
   * or is preparing to finish operating.
   *
   * @return true if finished
   */
  public boolean isStopped() {
    switch (this) {
      case STOPPED_FAILED:
      case STOPPED_SUCCESSFUL:
      case STOPPED_CANCELED:
      case STOPPED_ERROR:
        return true;
      default:
        return false;
    }
  }

  public boolean isStopping() {
    return switch (this) {
      case STOPPING_FAILED, STOPPING_SUCCESSFUL, STOPPING_CANCELED, STOPPING_ERROR -> true;
      default -> false;
    };
  }

  /**
   * Determine if this state is implying the process had or will have a successful result upon completion.
   *
   * @return true if successful
   */
  public boolean isSuccessful() {
    switch (this) {
      case STOPPED_SUCCESSFUL:
      case RUNNING_SUCCESSFUL:
        return true;
      default:
        return false;
    }
  }

  /**
   * Determine if the state signals a stopping state, whether a successful result had been found or not.
   *
   * @return true if stopping
   */
  public boolean shouldStop() {
    switch (this) {
      case STOPPING_FAILED:
      case STOPPING_SUCCESSFUL:
      case STOPPING_CANCELED:
      case STOPPING_ERROR:
      case STOPPED_CANCELED:
      case STOPPED_ERROR:
      case STOPPED_FAILED:
      case STOPPED_SUCCESSFUL:
        return true;
      default:
        return false;
    }
  }

  @Override
  public String toString() {
    return this.message;
  }

  public ResultState stoppingResult(boolean cancel) {
    switch (this) {
      case IDLE:
      case RUNNING:
        return cancel ? ResultState.STOPPING_CANCELED : ResultState.STOPPING_FAILED;
      case RUNNING_SUCCESSFUL:
        return ResultState.STOPPING_SUCCESSFUL;
      default:
        return this;
    }
  }

  /**
   * The result state of transitioning to a stopped state from the current state
   *
   * @return stopped result state
   */
  public ResultState stoppedResult() {
    switch (this) {
      case IDLE:
      case RUNNING:
      case STOPPING_FAILED:
        return STOPPED_FAILED;
      case RUNNING_SUCCESSFUL:
      case STOPPING_SUCCESSFUL:
        return STOPPED_SUCCESSFUL;
      case STOPPING_CANCELED:
        return STOPPED_CANCELED;
      case STOPPING_ERROR:
        return STOPPED_ERROR;
      default:
        // already is stopped
        return this;
    }
  }
}
