package net.whimxiqal.journey.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class MessageTemplate_2 extends MessageTemplate {
  MessageTemplate_2(String key) {
    super(key);
  }

  public Component resolve(TextColor color, Object element1, Object element2) {
    return super.resolve(color, Formatter.ACCENT, true, element1, element2);
  }

  public void sendTo(Audience audience, TextColor color, Object element1, Object element2) {
    audience.sendMessage(resolve(color, element1, element2));
  }

  @Override
  int numElements() {
    return 2;
  }
}
