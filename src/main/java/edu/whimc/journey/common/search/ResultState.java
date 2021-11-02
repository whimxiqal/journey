/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.journey.common.search;

/**
 * An enumeration of possible result states from running processes.
 */
public enum ResultState {

  IDLE,
  RUNNING,
  RUNNING_SUCCESSFUL,
  CANCELING_FAILED,
  CANCELING_SUCCESSFUL,
  STOPPED_FAILED,
  STOPPED_SUCCESSFUL,
  STOPPED_CANCELED;

  public boolean isRunning() {
    return this == RUNNING
        || this == RUNNING_SUCCESSFUL
        || this == CANCELING_FAILED
        || this == CANCELING_SUCCESSFUL;
  }

  public boolean hasFinished() {
    return this == STOPPED_FAILED
        || this == STOPPED_SUCCESSFUL
        || this == STOPPED_CANCELED
        || this == CANCELING_FAILED
        || this == CANCELING_SUCCESSFUL;
  }

  public boolean isSuccessful() {
    return this == STOPPED_SUCCESSFUL
        || this == RUNNING_SUCCESSFUL;
  }

  public boolean isCancelFailed() {
    return this == CANCELING_FAILED
        || this == STOPPED_CANCELED;
  }

  public boolean isCanceled() {
    return this == CANCELING_FAILED
        || this == CANCELING_SUCCESSFUL
        || this == STOPPED_CANCELED;
  }


  @Override
  public String toString() {
    return switch (this) {
      case IDLE -> "idle";
      case RUNNING -> "running";
      case RUNNING_SUCCESSFUL -> "running (successful)";
      case CANCELING_FAILED -> "canceling (failed)";
      case CANCELING_SUCCESSFUL -> "canceling (successful)";
      case STOPPED_FAILED -> "stopped (failed)";
      case STOPPED_SUCCESSFUL -> "stopped (successful)";
      case STOPPED_CANCELED -> "stopped (canceled)";
    };
  }
}
