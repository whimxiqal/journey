package edu.whimc.indicator.api.path;

import java.util.function.Predicate;

public interface Completion<T extends Locatable<T, D>, D> extends Predicate<Locatable<T, D>> {

}
