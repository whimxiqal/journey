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

package net.whimxiqal.journey.manager;

import java.util.HashMap;
import java.util.Map;
import net.whimxiqal.journey.Journey;

public class DomainManager {

  private final Map<Integer, String> indexToId = new HashMap<>();
  private final Map<String, Integer> idToIndex = new HashMap<>();

  public int domainIndex(String id) {
    Integer index = idToIndex.get(id);
    if (index == null) {
      index = idToIndex.size();
      indexToId.put(index, id);
      idToIndex.put(id, index);
    }
    return index;
  }

  public String domainId(int index) {
    String id = indexToId.get(index);
    if (id == null) {
      throw new IllegalArgumentException("There is no domain id with index: " + index);
    }
    return id;
  }

}
