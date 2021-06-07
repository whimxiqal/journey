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
import com.google.common.collect.Maps;
import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.data.DataAccessException;
import edu.whimc.indicator.common.util.Extra;
import edu.whimc.indicator.spigot.util.Format;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.permissions.Permission;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class CommandNode implements CommandExecutor, TabCompleter {

  public static final int ARG_MAX_LENGTH = 20;

  private final CommandNode parent;
  private final Permission permission;
  private final String description;
  private final List<String> aliases = Lists.newLinkedList();
  private final List<CommandNode> children = Lists.newLinkedList();
  private final Map<Parameter, String> parameters = Maps.newLinkedHashMap();
  @Setter
  @Getter
  private boolean canBypassInvalid = false;

  /**
   * Simple constructor.
   *
   * @param parent       parent node, null if none
   * @param permission   permission allowing this command
   * @param description  describes the function of this command
   * @param primaryAlias the primary label used to call this command
   */
  public CommandNode(@Nullable CommandNode parent,
                     @Nullable Permission permission,
                     @NotNull String description,
                     @NotNull String primaryAlias) {
    this(parent, permission, description, primaryAlias, true);

  }

  /**
   * Full constructor.
   *
   * @param parent       parent node, null if none
   * @param permission   permission allowing this command
   * @param description  describes the function of this command
   * @param primaryAlias the primary label used to call this command
   * @param addHelp      whether a help sub-command is generated
   */
  public CommandNode(@Nullable CommandNode parent,
                     @Nullable Permission permission,
                     @NotNull String description,
                     @NotNull String primaryAlias,
                     boolean addHelp) {
    Objects.requireNonNull(description);
    Objects.requireNonNull(primaryAlias);
    this.parent = parent;
    this.permission = permission;
    this.description = description;
    this.aliases.add(primaryAlias);
    if (addHelp) {
      this.children.add(new HelpCommandNode(this));
    }
  }

  // Getters and Setters

  @Nullable
  public final CommandNode getParent() {
    return parent;
  }

  @NotNull
  public final Optional<Permission> getPermission() {
    return Optional.ofNullable(permission);
  }

  @NotNull
  public final String getDescription() {
    return description;
  }

  @NotNull
  public final String getPrimaryAlias() {
    return aliases.get(0);
  }

  @NotNull
  @SuppressWarnings("unused")
  public final List<String> getAliases() {
    return aliases;
  }

  public final void addAliases(@NotNull String... aliases) {
    this.aliases.addAll(Arrays.asList(aliases));
  }

  @NotNull
  public final List<Parameter> getParameters() {
    return Lists.newLinkedList(parameters.keySet());
  }

  @NotNull
  public final String getParameterDescription(@NotNull Parameter parameter) {
    return parameters.get(parameter);
  }

  @SuppressWarnings("SameParameterValue")
  protected final void addSubcommand(@NotNull Parameter parameter, @NotNull String description) {
    this.parameters.put(parameter, description);
  }

  @NotNull
  public final String getFullCommand() {
    StringBuilder command = new StringBuilder(getPrimaryAlias());
    CommandNode cur = this;
    while (!cur.isRoot()) {
      command.insert(0, cur.parent.getPrimaryAlias() + " ");
      cur = cur.parent;
    }
    return command.toString();
  }

  protected final void addChildren(CommandNode... nodes) {
    this.children.addAll(Arrays.asList(nodes));
  }

  @NotNull
  public final List<CommandNode> getChildren() {
    return children;
  }

  public final boolean isRoot() {
    return parent == null;
  }

  public final void sendCommandError(CommandSender sender, String error) {
    sender.spigot().sendMessage(Format.error(error));
    // TODO figure out why this method has no hover/click event
    sender.spigot().sendMessage(Format.chain(Format.textOf(Format.PREFIX),
        Format.command("/" + getFullCommand() + " help",
            Format.DEFAULT + "Run help command")));
  }

  public final void sendCommandError(CommandSender sender, CommandError error) {
    sendCommandError(sender, error.getMessage());
  }

  @Override
  public final boolean onCommand(@NotNull CommandSender sender,
                                 @NotNull Command command,
                                 @NotNull String label,
                                 @NotNull String[] args) {

    if (!Indicator.getInstance().isValid() && !canBypassInvalid) {
      sender.spigot().sendMessage(Format.warn("The Indicator plugin is still initializing..."));
      return false;
    }

    String[] argsCombined = Extra.combineQuotedArguments(args);

    // Adds support for quotations around space-delimited arguments
    List<String> actualArgsList = new LinkedList<>();
    Set<String> flags = new HashSet<>();
    for (String arg : argsCombined) {
      if (arg.charAt(0) == '-' && !Extra.isNumber(arg.substring(1))) {
        flags.add(arg.substring(1));
      } else {
        actualArgsList.add(arg);
      }
    }

    if (isRoot()) {
      for (String arg : actualArgsList) {
        if (arg.length() > ARG_MAX_LENGTH) {
          sender.spigot().sendMessage(Format.error("Arguments cannot exceed "
              + ARG_MAX_LENGTH
              + " characters!"));
          return false;
        }
      }
    }
    String[] actualArgs = actualArgsList.toArray(new String[0]);
    return onCommand(sender, command, label, actualArgs, flags);
  }

  private boolean onCommand(@NotNull CommandSender sender,
                            @NotNull Command command,
                            @NotNull String label,
                            @NotNull String[] actualArgs,
                            @NotNull Set<String> flags) {
    if (this.permission != null && !sender.hasPermission(this.permission)) {
      sender.spigot().sendMessage(Format.error("You don't have permission to do this!"));
      return false;
    }
    if (actualArgs.length != 0) {
      for (CommandNode child : children) {
        for (String alias : child.aliases) {
          if (alias.equalsIgnoreCase(actualArgs[0])) {
            return child.onCommand(sender,
                command,
                child.getPrimaryAlias(),
                Arrays.copyOfRange(actualArgs, 1, actualArgs.length),
                flags);
          }
        }
      }
    }
    try {
      return onWrappedCommand(sender, command, label, actualArgs, flags);
    } catch (DataAccessException e) {
      sender.spigot().sendMessage(Format.error("An error occurred. Please contact an administrator."));
      return false;
    }
  }

  /**
   * Executes the given command, returning its success.
   * This command already filters for permission status and
   * traverses the command tree to give just the elements of the subcommand,
   * if it is not the root.
   * <p>
   * If false is returned, then the "usage" plugin.yml entry for this command
   * (if defined) will be sent to the player.
   *
   * @param sender  the command sender
   * @param command the originally executed command
   * @param label   the alias of the command or subcommand used
   * @param args    the arguments of this command or subcommand
   * @return true if the command succeeded, false if failed
   */
  public abstract boolean onWrappedCommand(@NotNull CommandSender sender,
                                           @NotNull Command command,
                                           @NotNull String label,
                                           @NotNull String[] args,
                                           @NotNull Set<String> flags) throws DataAccessException;

  @Override
  public final List<String> onTabComplete(@NotNull CommandSender sender,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args) {
    List<String> allPossible = Lists.newLinkedList();
    if (this.permission != null && !sender.hasPermission(this.permission)) {
      return allPossible; // empty
    }
    if (args.length == 0) {
      return allPossible; // empty
    }
    for (CommandNode child : children) {
      for (int i = 0; i < child.aliases.size(); i++) {
        String alias = child.aliases.get(i);

        // If any alias matches from this child, then bump us up to its children
        if (alias.equalsIgnoreCase(args[0])) {
          return child.onTabComplete(sender,
              command,
              child.getPrimaryAlias(),
              Arrays.copyOfRange(args, 1, args.length));
        }

        // Only if we're on the last arg of the recursion and we're at the primary alias,
        // and we have permission to the command, add it
        if (args.length == 1 && i == 0) {
          if (child.permission == null || sender.hasPermission(child.permission)) {
            allPossible.add(alias);
          }
        }
      }
    }

    for (Parameter param : parameters.keySet()) {
      allPossible.addAll(param.nextAllowedInputs(sender, Arrays.copyOfRange(args, 0, args.length - 1)));
    }

    List<String> out = Lists.newLinkedList();
    StringUtil.copyPartialMatches(args[args.length - 1], allPossible, out);
    Collections.sort(out);
    return out;
  }

}
