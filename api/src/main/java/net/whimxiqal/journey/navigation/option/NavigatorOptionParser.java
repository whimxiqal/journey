package net.whimxiqal.journey.navigation.option;

@FunctionalInterface
public interface NavigatorOptionParser<T> {

  T parse(String serialized) throws ParseNavigatorOptionException;

}
