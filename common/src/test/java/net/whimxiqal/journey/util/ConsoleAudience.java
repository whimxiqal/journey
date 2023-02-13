package net.whimxiqal.journey.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class ConsoleAudience implements Audience {

  @Override
  public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
    System.out.println(PlainTextComponentSerializer.plainText().serialize(message));
  }

}
