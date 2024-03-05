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

package net.whimxiqal.journey.util;

import net.whimxiqal.journey.Journey;
import org.bstats.charts.CustomChart;
import org.bstats.charts.SingleLineChart;

public class BStatsUtil {

  public static final String SEARCHES_PER_HOUR_ID = "searches";
  public static final String BLOCKS_TRAVELLED_PER_HOUR_ID = "blocks_travelled";

  private BStatsUtil() {
  }

  public static CustomChart[] charts() {
    return new CustomChart[]{
        new SingleLineChart(SEARCHES_PER_HOUR_ID, () -> Journey.get().statsManager().searches()),
        new SingleLineChart(BLOCKS_TRAVELLED_PER_HOUR_ID, () -> Journey.get().statsManager().blocksTravelled())
    };
  }

}
