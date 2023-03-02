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

package net.whimxiqal.journey.scope;

import java.util.Optional;
import net.whimxiqal.journey.search.SearchSession;

public class ScopedSessionResult {

  public enum Type {
    EXISTS,
    NONE,
    NO_SCOPE,
    AMBIGUOUS,
    NO_PERMISSION
  }

  public static class AmbiguousItem {
    public final String scope1;
    public final String scope2;

    public AmbiguousItem(String scope1, String scope2) {
      this.scope1 = scope1;
      this.scope2 = scope2;
    }
  }

  private final Type type;  // type of result
  private final SearchSession session;  // EXISTS: cell is result, else null
  private final String scope;  // EXISTS: scope on which the result was found, else null
  private final String missing;  // NO_SCOPE: component that's missing, could be scope or item, else null
  private final AmbiguousItem ambiguousItem;  // AMBIGUOUS: ambiguous item info, else null

  public static ScopedSessionResult exists(SearchSession destination, String scope) {
    return new ScopedSessionResult(Type.EXISTS, destination, scope, null, null);
  }

  public static ScopedSessionResult noScope(String scope) {
    return new ScopedSessionResult(Type.NO_SCOPE, null, null, scope, null);
  }

  public static ScopedSessionResult none() {
    return new ScopedSessionResult(Type.NONE, null, null, null, null);
  }

  public static ScopedSessionResult ambiguous(String scope1, String scope2) {
    return new ScopedSessionResult(Type.AMBIGUOUS, null, null, null, new AmbiguousItem(scope1, scope2));
  }

  public static ScopedSessionResult noPermission() {
    return new ScopedSessionResult(Type.NO_PERMISSION, null, null, null, null);
  }

  public ScopedSessionResult(Type type, SearchSession session, String scope, String missing, AmbiguousItem ambiguousItem) {
    this.type = type;
    this.session = session;
    this.scope = scope;
    this.missing = missing;
    this.ambiguousItem = ambiguousItem;
  }

  public Type type() {
    return type;
  }

  public Optional<SearchSession> session() {
    return Optional.ofNullable(session);
  }

  public Optional<String> scope() {
    return Optional.ofNullable(scope);
  }

  public Optional<String> missing() {
    return Optional.ofNullable(missing);
  }

  public Optional<AmbiguousItem> ambiguousItem() {
    return Optional.ofNullable(ambiguousItem);
  }

}
