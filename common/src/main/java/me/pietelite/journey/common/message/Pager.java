/*
 * MIT License
 *
 * Copyright 2022 Pieter Svenson
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
 *
 */

package me.pietelite.journey.common.message;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

public class Pager {

  private static final int MESSAGES_PER_PAGE = 7;

  private final List<Component> components = new LinkedList<>();

  private Pager(Collection<Component> components) {
    this.components.addAll(components);
  }

  public static Pager of(Collection<Component> components) {
    return new Pager(components);
  }

  public static <T> Pager of(Collection<T> components,
                             Function<T, String> keyExtractor,
                             Function<T, String> valueExtractor) {
    return of(components.stream().map(entry -> Component.text()
            .append(Component.text(keyExtractor.apply(entry)).color(Formatter.ACCENT))
            .append(Component.text(" > ").color(Formatter.WHITE))
            .append(Component.text(valueExtractor.apply(entry)).color(Formatter.INFO))
            .build())
        .collect(Collectors.toList()));
  }

  public Pager add(Component line) {
    this.components.add(line);
    return this;
  }

  public void sendPage(Audience audience, int page) {
    // page = page index + 1
    components.subList((page - 1) * MESSAGES_PER_PAGE,
            Math.min(components.size(), page * MESSAGES_PER_PAGE))
        .forEach(audience::sendMessage);
  }

}
