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

package edu.whimc.indicator.common.search;

/**
 * An enumeration of possible result states from running processes.
 */
public enum ResultState {

  IDLE,
  FAILED,
  SUCCESSFUL,
  RUNNING,
  CANCELING,
  CANCELED;

  /**
   * If the state it not {@link ResultState#IDLE}.
   *
   * @return true if not idle
   */
  public boolean hasStarted() {
    return this != IDLE;
  }

  /**
   * If the state is {@link ResultState#RUNNING}.
   *
   * @return true if running
   */
  public boolean isRunning() {
    return this == RUNNING || this == CANCELING;
  }

  /**
   * If the result is {@link ResultState#FAILED}, {@link ResultState#SUCCESSFUL},
   * or {@link ResultState#CANCELED}.
   *
   * @return true if the state is finished.
   */
  public boolean hasFinished() {
    return this == FAILED || this == SUCCESSFUL || this == CANCELED;
  }

  /**
   * If the result is {@link ResultState#SUCCESSFUL}.
   *
   * @return true if the state is successful.
   */
  public boolean isSuccessful() {
    return this == SUCCESSFUL;
  }

  /**
   * If the result is {@link ResultState#FAILED} or {@link ResultState#CANCELED}.
   * Otherwise, the state is either successful or undetermined.
   *
   * @return true if determined to be not successful
   */
  public boolean isUnsuccessful() {
    return this == FAILED || this == CANCELING || this == CANCELED;
  }

  /**
   * If the result is either canceled or in the process of being canceled.
   *
   * @return true if canceled or cancelling
   */
  public boolean isCanceled() {
    return this == CANCELING || this == CANCELED;
  }

}
