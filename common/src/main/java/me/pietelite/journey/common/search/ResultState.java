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

package me.pietelite.journey.common.search;

/**
 * An enumeration of possible result states from running processes.
 */
public enum ResultState {

  IDLE("idle"),
  RUNNING("running"),
  RUNNING_SUCCESSFUL("running (successful)"),
  CANCELING_FAILED("canceling (failed)"),
  CANCELING_SUCCESSFUL("canceling (successful)"),
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
      case CANCELING_FAILED:
      case CANCELING_SUCCESSFUL:
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
   * Determine if this state is implying the process was canceled/stopped before it was determined successful,
   * whether it would have been successful if it was allowed to continue or not.
   *
   * @return true if canceled and consequently failed
   */
  public boolean isCancelFailed() {
    switch (this) {
      case CANCELING_FAILED:
      case STOPPED_CANCELED:
        return true;
      default:
        return false;
    }
  }

  /**
   * Determine if this state was canceled at any point, whether a successful result had been found or not.
   *
   * @return true if canceled (manually stopped)
   */
  public boolean shouldStop() {
    switch (this) {
      case CANCELING_FAILED:
      case CANCELING_SUCCESSFUL:
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

  /**
   * The result state of transitioning to a stopped state from the current state
   * @return stopped result state
   */
  public ResultState stoppedResult() {
    switch (this) {
      case IDLE:
      case RUNNING:
        return STOPPED_ERROR;
      case RUNNING_SUCCESSFUL:
      case CANCELING_SUCCESSFUL:
        return STOPPED_SUCCESSFUL;
      case CANCELING_FAILED:
        return STOPPED_FAILED;
      default:
        // already is stopped
        return this;
    }
  }
}
