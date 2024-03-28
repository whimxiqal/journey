package net.whimxiqal.journey.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class MessageTemplate_3 extends MessageTemplate {
  MessageTemplate_3(String key) {
    super(key);
  }

  public Component resolve(TextColor color, Object element1, Object element2, Object element3) {
    return super.resolve(color, Formatter.ACCENT, true, element1, element2, element3);
  }

  public void sendTo(Audience audience, TextColor color, Object element1, Object element2, Object element3) {
    audience.sendMessage(resolve(color, element1, element2, element3));
  }

  @Override
  int numElements() {
    return 3;
  }
}
