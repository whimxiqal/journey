package net.whimxiqal.journey.navigation.option;

@FunctionalInterface
public interface NavigatorOptionValidator<T> {

  String validate(T value);

}
