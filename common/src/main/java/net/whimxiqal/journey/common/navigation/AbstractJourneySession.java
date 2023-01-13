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

package net.whimxiqal.journey.common.navigation;

import net.whimxiqal.journey.common.navigation.journey.JourneySession;
import net.whimxiqal.journey.common.search.SearchSession;
import net.whimxiqal.journey.common.tools.AlternatingList;
import org.jetbrains.annotations.NotNull;

/**
 * A general implementation of a {@link JourneySession} specifically for Spigot purposes.
 *
 * @deprecated we should not be using this weird class anymore, let's try to streamline this
 */
public abstract class AbstractJourneySession implements JourneySession {

  private final SearchSession session;
  private final Itinerary itinerary;
  private AlternatingList.Traversal<Port, Path, Path> traversal;
  /**
   * Itinerary that is queued as a better one, in case the player wants to use it.
   */
  private Itinerary prospectiveItinerary;

  /**
   * General constructor.
   *
   * @param session   the session ultimately causing this journey
   * @param itinerary the itinerary that acts a roadmap for this journey
   */
  public AbstractJourneySession(@NotNull SearchSession session,
                                @NotNull Itinerary itinerary) {
    this.session = session;
    this.itinerary = itinerary;
    this.traversal = itinerary.getStages().traverse();
  }

  /**
   * Get the player search session used to calculate this journey.
   *
   * @return the session
   */
  public SearchSession getSession() {
    return session;
  }

  /**
   * Get the itinerary that originally determined the directions
   * and acts as a roadmap for this journey.
   *
   * @return the itinerary
   */
  public Itinerary getItinerary() {
    return this.itinerary;
  }

  /**
   * Get the prospective itinerary.
   * This (better) itinerary was calculated by the session after this journey was already prepared
   * for the user. The user will have the option of using this itinerary if he/she
   * so chooses and create another journey.
   *
   * @return the itinerary
   */
  public Itinerary getProspectiveItinerary() {
    return prospectiveItinerary;
  }

  /**
   * Get the prospective itinerary.
   * This (better) itinerary was calculated by the session after this journey was already prepared
   * for the user. The user will have the option of using this itinerary if he/she
   * so chooses and create another journey.
   *
   * @param prospectiveItinerary the prospective itinerary
   */
  public void setProspectiveItinerary(Itinerary prospectiveItinerary) {
    this.prospectiveItinerary = prospectiveItinerary;
  }

  /**
   * Get the destination of the current path being traversed.
   *
   * @return the destination location
   */
  public final Cell currentPathDestination() {
    return traversal.get().getDestination();
  }

  protected final AlternatingList.Traversal<Port,
      Path,
      Path> traversal() {
    return traversal;
  }

  protected final void resetTraversal() {
    this.traversal = getItinerary().getStages().traverse();
  }

}
