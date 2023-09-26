package net.whimxiqal.journey.navigation;

import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.navigation.option.NavigatorOption;

public abstract class NavigatorDetailsBuilderImpl<B extends NavigatorDetailsBuilder<B>> implements NavigatorDetailsBuilder<B> {

  private final String navigatorType;
  protected final Map<String, Object> options = new HashMap<>();

  protected NavigatorDetailsBuilderImpl(String navigatorType) {
    this.navigatorType = navigatorType;
  }

  @Override
  public NavigatorDetails build() {
    return new NavigatorDetails(navigatorType, options);
  }

  @Override
  public B setOption(String key, Object value) {
    options.put(key, value);
    return getDerived();
  }

  @Override
  public <T> B setOption(NavigatorOption<T> option, T value) {
    options.put(option.optionId(), value);
    return getDerived();
  }

  @Override
  public B completionMessage(Component message) {
    options.put(NavigationManager.NAVIGATOR_OPTION_ID_COMPLETION_MESSAGE, message);
    return getDerived();
  }

  @Override
  public B completionTitle(Component title) {
    options.put(NavigationManager.NAVIGATOR_OPTION_ID_COMPLETION_TITLE, title);
    return getDerived();
  }

  @Override
  public B completionSubtitle(Component subtitle) {
    options.put(NavigationManager.NAVIGATOR_OPTION_ID_COMPLETION_SUBTITLE, subtitle);
    return getDerived();
  }

  protected abstract B getDerived();

  public static class Self extends NavigatorDetailsBuilderImpl<Self> {
    public Self(String navigatorType) {
      super(navigatorType);
    }

    @Override
    protected Self getDerived() {
      return this;
    }

  }
}
