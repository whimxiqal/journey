package edu.whimc.indicator.api.path;

import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;

public class Path<T extends Locatable<T, D>, D> {

    private final LinkedList<Trail<T, D>> localTrails = Lists.newLinkedList();
    private final LinkedList<Link<T, D>> domainLinks = Lists.newLinkedList();

    public boolean addLinkedTrail(Trail<T, D> trail, Link<T, D> link) {
        this.localTrails.add(trail);
        this.domainLinks.add(link);
        return true;
    }

    public boolean addFinalTrail(Trail<T, D> trail) {
        if (localTrails.size() > domainLinks.size()) {
            // There are more trails than links to a final trail must have already been added.
            return false;
        }
        this.localTrails.add(trail);
        return true;
    }

    public T getOrigin() {
        if (localTrails.isEmpty() || localTrails.getFirst().getSteps().isEmpty()) {
            return null;
        }
        return localTrails.getFirst().getSteps().getFirst();
    }

    public T getDestination() {
        if (localTrails.isEmpty() || localTrails.getLast().getSteps().isEmpty()) {
            return null;
        }
        return localTrails.getLast().getSteps().getLast();
    }

    public List<T> getAllSteps() {
        LinkedList<T> allSteps = new LinkedList<>();
        for (Trail<T, D> trail : localTrails) {
            allSteps.addAll(trail.getSteps());
        }
        return allSteps;
    }


}
