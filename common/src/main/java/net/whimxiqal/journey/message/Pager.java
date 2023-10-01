/*
 * MIT License
 *
 * Copyright (c) whimxiqal
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

package net.whimxiqal.journey.message;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

public class Pager {

  private static final int MESSAGES_PER_PAGE = 7;

  private final Component header;
  private final List<Component> components = new LinkedList<>();
  private final int totalPages;

  private Pager(Component header, Collection<Component> components) {
    this.header = header;
    this.components.addAll(components);
    this.totalPages = components.size() / MESSAGES_PER_PAGE + (components.size() % MESSAGES_PER_PAGE == 0 ? 0 : 1);
  }

  public static Pager of(Component header, Collection<Component> components) {
    return new Pager(header, components);
  }

  public static <T> Pager of(Component header,
                             Collection<T> components,
                             Function<T, Component> keyExtractor,
                             Function<T, Component> valueExtractor) {
    return of(header, components.stream().map(entry -> Component.text()
            .append(keyExtractor.apply(entry))
            .append(Component.text(" > ").color(Formatter.DARK))
            .append(valueExtractor.apply(entry))
            .build())
        .collect(Collectors.toList()));
  }

  public Pager add(Component line) {
    this.components.add(line);
    return this;
  }

  /**
   * Send page to the audience.
   *
   * @param audience the audience
   * @param page     the page number, indexed starting at 1
   */
  public void sendPage(Audience audience, int page) {
    // page = page index + 1
    if (page < 1) {
      throw new IllegalArgumentException("Page must be greater than 0");
    }
    page = Math.min(page, totalPages);
    audience.sendMessage(header.append(Component.text(" % ").color(Formatter.DARK))
        .append(Component.text("page ").color(Formatter.DULL))
        .append(Formatter.accent(Integer.toString(page)))
        .append(Component.text(" / " + totalPages).color(Formatter.DULL)));
    components.subList((page - 1) * MESSAGES_PER_PAGE,
            Math.min(components.size(), page * MESSAGES_PER_PAGE))
        .forEach(audience::sendMessage);
  }

}
