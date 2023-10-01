package net.whimxiqal.journey.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class MessageTemplate_0 extends MessageTemplate {
  MessageTemplate_0(String key) {
    super(key);
  }

  public Component resolve(TextColor color) {
    return super.resolve(color, null, true);
  }

  public String resolve() {
    return super.resolve();
  }

  public void sendTo(Audience audience, TextColor color) {
    audience.sendMessage(resolve(color));
  }

  @Override
  int numElements() {
    return 0;
  }
}
