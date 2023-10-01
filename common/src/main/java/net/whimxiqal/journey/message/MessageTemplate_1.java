package net.whimxiqal.journey.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class MessageTemplate_1 extends MessageTemplate {
  MessageTemplate_1(String key) {
    super(key);
  }

  public Component resolve(TextColor color, Object element) {
    return super.resolve(color, Formatter.ACCENT, true, element);
  }

  public String resolve(String element) {
    return super.resolve(element);
  }

  public void sendTo(Audience audience, TextColor color, Object element) {
    audience.sendMessage(resolve(color, element));
  }

  @Override
  int numElements() {
    return 1;
  }
}
