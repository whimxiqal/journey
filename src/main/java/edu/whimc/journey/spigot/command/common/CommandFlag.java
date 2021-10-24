package edu.whimc.journey.spigot.command.common;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandFlag {

  @Getter
  private final String key;

  public boolean isIn(Map<String, String> flagMap) {
    return flagMap.containsKey(key);
  }

}
