/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.indicator.spigot.command.common;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Builder
public class Parameter {

  @NonNull @Builder.Default
  private final ParameterSupplier supplier = ParameterSuppliers.NONE;
  private final Permission permission;
  private Parameter next;

  public static Parameter basic(String usage) {
    return Parameter.builder()
        .supplier(ParameterSupplier.builder().usage(usage).build())
        .build();
  }

  /**
   * Gives the supplier of possible accepted inputs for this parameter.
   * If there is no supplier, then the constant value NONE in
   * {@link ParameterSuppliers} is given instead.
   *
   * @return an appropriate {@link ParameterSupplier}
   */
  public ParameterSupplier getSupplier() {
    return supplier;
  }

  public Optional<Permission> getPermission() {
    return Optional.ofNullable(permission);
  }

  private Optional<Parameter> getNext() {
    return Optional.ofNullable(next);
  }

  /**
   * Get the list of allowed inputs after the given set of inputs.
   *
   * @param sender  the sender of the command with parameters
   * @param toParse the list of inputs to parse
   * @return a list of allowed next inputs
   */
  @NotNull
  public Collection<String> nextAllowedInputs(@NotNull CommandSender sender, @NotNull String[] toParse) {
    return nextAllowedInputsHelper(sender, toParse, new String[0]);
  }

  private Collection<String> nextAllowedInputsHelper(@NotNull CommandSender sender,
                                                     @NotNull String[] toParse,
                                                     @NotNull String[] previous) {
    if (getPermission().isPresent() && !sender.hasPermission(this.permission)) {
      return Lists.newLinkedList();
    }
    if (toParse.length == 0) {
      return supplier.getAllowedEntries(Arrays.asList(previous));
    }
    if (this.supplier.matches(Arrays.asList(previous), toParse[0])) {
      String[] newPrevious = Arrays.copyOf(previous, previous.length + 1);
      newPrevious[newPrevious.length - 1] = toParse[0];
      return getNext()
          .map(next ->
              next.nextAllowedInputsHelper(
                  sender,
                  Arrays.copyOfRange(toParse, 1, toParse.length),
                  newPrevious))
          .orElse(Lists.newArrayList());
    }
    return Lists.newLinkedList();
  }

  /**
   * Create a single parameter by putting the given parameters in series.
   *
   * @param first  the necessary first parameter
   * @param others the following parameters
   * @return a single merged parameter
   */
  public static Parameter chain(@NotNull Parameter first, @NotNull Parameter... others) {
    Objects.requireNonNull(first);
    Objects.requireNonNull(others);
    List<Parameter> parameters = Lists.newArrayList(first);
    parameters.addAll(Arrays.asList(others));
    for (int i = 0; i < parameters.size() - 1; i++) {
      Parameter cur = parameters.get(i);
      while (cur.getNext().isPresent()) {
        cur = cur.getNext().get();
      }
      cur.next = parameters.get(i + 1);
    }
    return parameters.get(0);
  }

  /**
   * A string combining all individual parameters in series.
   *
   * @param sender the command sender
   * @return a wrapped string representing the parameter's usage
   */
  public Optional<String> getFullUsage(@NotNull CommandSender sender) {
    if (getPermission().isPresent() && !sender.hasPermission(getPermission().get())) {
      return Optional.empty();
    }
    StringBuilder builder = new StringBuilder();
    Parameter cur = this;
    builder.append(cur.getSupplier().getUsage());
    while (cur.getNext().isPresent()
        && cur.getNext().get().getPermission().map(sender::hasPermission).orElse(true)) {
      cur = cur.next;
      builder.append(" ");
      builder.append(cur.getSupplier().getUsage());
    }
    return Optional.of(builder.toString());
  }

  @Builder
  public static class ParameterSupplier {

    @Builder.Default
    private final Function<List<String>, List<String>> allowedEntries = prev -> Lists.newLinkedList();
    @Getter
    @Builder.Default
    private final String usage = "";
    // True if this parameter is only correct if it's in the allowedEntries
    @Getter
    @Builder.Default
    private boolean strict = true;

    public Collection<String> getAllowedEntries(List<String> previousParameters) {
      return allowedEntries.apply(previousParameters);
    }

    public boolean matches(List<String> previousParameters, String input) {
      List<String> allowedEntriesApplied = allowedEntries.apply(previousParameters);
      return !strict || allowedEntriesApplied.isEmpty() || allowedEntriesApplied.stream().anyMatch(allowed -> allowed.equalsIgnoreCase(input));
    }

  }

}
