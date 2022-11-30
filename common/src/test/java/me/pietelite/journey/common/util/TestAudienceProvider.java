/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pietelite.journey.common.util;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class TestAudienceProvider implements AudienceProvider {
  @Override
  public @NotNull Audience all() {
    return Audience.empty();
  }

  @Override
  public @NotNull Audience console() {
    return new Audience() {
      @Override
      public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        System.out.println(PlainTextComponentSerializer.plainText().serialize(message));
      }
    };
  }

  @Override
  public @NotNull Audience players() {
    return Audience.empty();
  }

  @Override
  public @NotNull Audience player(@NotNull UUID playerId) {
    return Audience.empty();
  }

  @Override
  public @NotNull Audience permission(@NotNull String permission) {
    return Audience.empty();
  }

  @Override
  public @NotNull Audience world(@NotNull Key world) {
    return Audience.empty();
  }

  @Override
  public @NotNull Audience server(@NotNull String serverName) {
    return Audience.empty();
  }

  @Override
  public @NotNull ComponentFlattener flattener() {
    return ComponentFlattener.basic();
  }

  @Override
  public void close() {
    // do nothing
  }
}
