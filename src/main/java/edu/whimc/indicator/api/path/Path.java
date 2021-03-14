package edu.whimc.indicator.api.path;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public final class Path<T extends Locatable<T, D>, D> {

  private final List<Trail<T, D>> trails = Lists.newArrayList();
  private final List<Link<T, D>> domainLinks = Lists.newArrayList();

  public boolean addLinkedTrail(Trail<T, D> trail, Link<T, D> link) {
    this.trails.add(trail);
    this.domainLinks.add(link);
    return true;
  }

  public boolean addFinalTrail(Trail<T, D> trail) {
    if (trails.size() > domainLinks.size()) {
      // There are more trails than links to a final trail must have already been added.
      return false;
    }
    this.trails.add(trail);
    return true;
  }

  public T getOrigin() {
    List<Step<T, D>> steps = trails.get(0).getSteps();
    if (trails.isEmpty() || steps.isEmpty()) {
      return null;
    }
    return steps.get(0).getLocatable();
  }

  public T getDestination() {
    List<Step<T, D>> steps = trails.get(trails.size() - 1).getSteps();
    if (trails.isEmpty() || steps.isEmpty()) {
      return null;
    }
    return steps.get(steps.size() - 1).getLocatable();
  }

  public List<Trail<T, D>> getTrailsCopy() {
    return new ArrayList<>(trails);
  }

  public List<Link<T, D>> getDomainLinksCopy() {
    return new ArrayList<>(domainLinks);
  }

  /**
   * Creates an (array) list of every step, ignoring links.
   *
   * @return all steps in a list
   */
  public List<Step<T, D>> getAllSteps() {
    ArrayList<Step<T, D>> allSteps = new ArrayList<>();
    for (Trail<T, D> trail : trails) {
      allSteps.addAll(trail.getSteps());
    }
    return allSteps;
  }

}
