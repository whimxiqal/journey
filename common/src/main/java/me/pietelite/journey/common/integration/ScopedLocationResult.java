package me.pietelite.journey.common.integration;

import java.util.Optional;
import me.pietelite.journey.common.navigation.Cell;

public class ScopedLocationResult {

  public enum Type {
    EXISTS,
    NONE,
    NO_SCOPE,
    AMBIGUOUS
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
  private final Cell cell;  // EXISTS: cell is result, else null
  private final String scope;  // EXISTS: scope on which the result was found, else null
  private final String missing;  // NO_SCOPE: component that's missing, could be scope or item, else null
  private final AmbiguousItem ambiguousItem;  // AMBIGUOUS: ambiguous item info, else null

  public static ScopedLocationResult exists(Cell cell, String scope) {
    return new ScopedLocationResult(Type.EXISTS, cell, scope, null, null);
  }

  public static ScopedLocationResult noScope(String scope) {
    return new ScopedLocationResult(Type.NO_SCOPE, null, null, scope, null);
  }

  public static ScopedLocationResult none() {
    return new ScopedLocationResult(Type.NONE, null, null, null, null);
  }

  public static ScopedLocationResult ambiguous(String scope1, String scope2) {
    return new ScopedLocationResult(Type.AMBIGUOUS, null, null, null, new AmbiguousItem(scope1, scope2));
  }

  public ScopedLocationResult(Type type, Cell cell, String scope, String missing, AmbiguousItem ambiguousItem) {
    this.type = type;
    this.cell = cell;
    this.scope = scope;
    this.missing = missing;
    this.ambiguousItem = ambiguousItem;
  }

  public Type type() {
    return type;
  }

  public Optional<Cell> location() {
    return Optional.ofNullable(cell);
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
