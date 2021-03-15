package edu.whimc.indicator.api.journey;

import edu.whimc.indicator.api.path.Completion;
import edu.whimc.indicator.api.path.Locatable;
import edu.whimc.indicator.api.path.Path;
import edu.whimc.indicator.api.path.Step;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

/**
 * Manage information about the traversal of locatables
 * within the game.
 *
 * @param <T> the type of locatable
 * @param <D> the type of domain
 */
public interface Journey<T extends Locatable<T, D>, D> {

  /**
   * Notify this {@link Journey} that the given {@link Locatable}
   * has been visited. This may be called very often, so efficiency
   * is important here.
   *
   * @param locatable the visited locatable
   */
  void visit(T locatable);

  /**
   * Give the next locatables to traverse along the journey.
   *
   * @param count the number of locatables to get
   * @return a collection of locatables of size {@code count}
   */
  Collection<Step<T, D>> next(int count);

  /**
   * Get the closest {@link Locatable} on this Journey to this other one.
   *
   * @param other the other locatable
   * @return the closest locatable
   */
  T closest(T other);

  boolean isCompleted();

}
