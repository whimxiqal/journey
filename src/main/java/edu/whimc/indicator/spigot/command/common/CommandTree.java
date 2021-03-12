/*
 * MIT License
 *
 * Copyright (c) 2020 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package edu.whimc.indicator.spigot.command.common;

import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

public class CommandTree {

  private CommandNode root;

  public CommandTree(@NotNull CommandNode root) {
    this.root = Objects.requireNonNull(root);
  }

  public CommandNode root() {
    return root;
  }

  public <N extends CommandNode> Optional<N> getNode(Class<N> clazz) {
    Stack<CommandNode> commands = new Stack<>();
    commands.push(root());
    while (!commands.isEmpty()) {
      CommandNode top = commands.peek();
      if (clazz.isInstance(top)) {
        return Optional.of(clazz.cast(top));
      }
      commands.pop();
      commands.addAll(top.getChildren());
    }
    return Optional.empty();
  }

  /**
   * Register all functionality of a command, noted in the plugin.yml.
   *
   * @param plugin the plugin under which this command is registered
   * @return the tree of all commands with the given root
   */
  public void register(@NotNull JavaPlugin plugin) {
    PluginCommand command = plugin.getCommand(root.getPrimaryAlias());
    if (command == null) {
      throw new NullPointerException("You must register this command in the plugin.yml");
    }
    command.setExecutor(root);
    command.setTabCompleter(root);
    root.getPermission().map(Permission::getName).ifPresent(command::setPermission);
  }

}

