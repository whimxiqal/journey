package net.whimxiqal.journey.navigation.option;

public interface NavigatorOptionValues {

  <T> T value(NavigatorOption<T> option) throws IllegalArgumentException;

  <T> T value(String optionId, Class<T> type) throws IllegalArgumentException;

}
