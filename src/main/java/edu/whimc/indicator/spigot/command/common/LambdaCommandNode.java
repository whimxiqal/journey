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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;

public class LambdaCommandNode extends CommandNode {

  private final BiFunction<CommandSender, String[], Boolean> executor;

  public LambdaCommandNode(@Nullable CommandNode parent,
                           @Nullable Permission permission,
                           @NotNull String description,
                           @NotNull String primaryAlias,
                           @NotNull BiFunction<CommandSender, String[], Boolean> executor) {
    super(parent, permission, description, primaryAlias);
    this.executor = Objects.requireNonNull(executor);
  }

  public LambdaCommandNode(@Nullable CommandNode parent,
                           @Nullable Permission permission,
                           @NotNull String description,
                           @NotNull String primaryAlias,
                           boolean addHelp,
                           @NotNull BiFunction<CommandSender, String[], Boolean> executor) {
    super(parent, permission, description, primaryAlias, addHelp);
    this.executor = Objects.requireNonNull(executor);
  }

  @Override
  public final boolean onWrappedCommand(@NotNull CommandSender sender,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args) {
    return executor.apply(sender, args);
  }
}
