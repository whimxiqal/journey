package me.pietelite.journey.common.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import me.pietelite.mantle.common.Logger;
import me.pietelite.mantle.common.Proxy;

public class TestProxy implements Proxy {

  private final TestLogger logger = new TestLogger();
  private final Map<UUID, Set<String>> permissions = new HashMap<>();

  @Override
  public Logger logger() {
    return logger;
  }

  @Override
  public boolean hasPermission(UUID uuid, String s) throws NoSuchElementException {
    return false;
  }

  public void setPermission(UUID uuid, String perm) {
    permissions.computeIfAbsent(uuid, k -> new HashSet<>()).add(perm);
  }

  public void unsetPermission(UUID uuid, String perm) {
    Set<String> perms = permissions.get(uuid);
    if (perms != null) {
      perms.remove(perm);
    }
  }

  @Override
  public List<String> onlinePlayerNames() {
    return Collections.emptyList();
  }

  @Override
  public List<String> worldNames() {
    return Collections.emptyList();
  }
}
