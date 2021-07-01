package edu.whimc.indicator.spigot.command.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class Flag {

  @Getter
  private final String key;

  public boolean isIn(Map<String, String> flagMap) {
    return flagMap.containsKey(key);
  }

}
