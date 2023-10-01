package net.whimxiqal.journey.navigation;

import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.Builder;
import net.whimxiqal.journey.navigation.option.NavigatorOption;

/**
 * A builder for {@link NavigatorDetails}.
 *
 * @param <B> the derived type of builder, for more helpful chaining
 */
public interface NavigatorDetailsBuilder<B extends NavigatorDetailsBuilder<B>>
    extends Builder<NavigatorDetails> {

  /**
   * Set a new option value.
   *
   * @param key   the option id
   * @param value the option value
   * @return the builder, for chaining
   */
  B setOption(String key, Object value);

  /**
   * Set a new option value using the option and type safety.
   *
   * @param option the option
   * @param value  the value
   * @param <T>    the type of the option's value
   * @return the builder, for chaining
   */
  <T> B setOption(NavigatorOption<T> option, T value);

  /**
   * Set the completion message, which will be sent to the agent upon completion of the navigator.
   *
   * @param message the message
   * @return the builder, for chaining
   */
  B completionMessage(Component message);

  /**
   * Set the completion title, which will be sent to the agent upon completion of the navigator.
   *
   * @param title the title
   * @return the builder, for chaining
   */
  B completionTitle(Component title);

  /**
   * Set the completion subtitle, which will be sent to the agent upon completion of the navigator.
   *
   * @param subtitle the subtitle
   * @return the builder, for chaining
   */
  B completionSubtitle(Component subtitle);

}
