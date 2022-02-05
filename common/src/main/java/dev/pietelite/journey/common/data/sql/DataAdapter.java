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

package dev.pietelite.journey.common.data.sql;

import dev.pietelite.journey.common.navigation.Cell;
import org.jetbrains.annotations.NotNull;

/**
 * An adapter for the data types put into storage.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public interface DataAdapter<T extends Cell<T, D>, D> {

  /**
   * Get the string identifier for a domain.
   *
   * @param domain the domain
   * @return the string identifier
   */
  @NotNull
  String getDomainIdentifier(@NotNull D domain);

  /**
   * Create a cell using the data required to create one:
   * three coordinates and a domain identifier.
   *
   * @param x        the x coordinate
   * @param y        the y coordinate
   * @param z        the z coordinate
   * @param domainId the domain identifier
   * @return a new cell
   */
  @NotNull
  T makeCell(int x, int y, int z, @NotNull String domainId);

}