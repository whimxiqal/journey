package net.whimxiqal.journey.spigot.integration;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.WarpNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.whimxiqal.journey.common.integration.Integrator;
import net.whimxiqal.journey.common.integration.Scope;
import net.whimxiqal.journey.common.integration.ScopeBuilder;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.spigot.util.SpigotUtil;
import net.whimxiqal.mantle.common.CommandSource;
import net.ess3.api.InvalidWorldException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class EssentialsIntegrator implements Integrator {
  @Override
  public String name() {
    return "essentials";
  }

  @Override
  public Scope scope(CommandSource src) {
    assert src.type() == CommandSource.Type.PLAYER;
    IEssentials essentials = essentials();
    ScopeBuilder builder = Scope.builder("essentials")
        .subScope(Scope.builder("warp")
            .items(() -> {
              Map<String, Cell> warps = new HashMap<>();
              for (String warp : essentials.getWarps().getList()) {
                try {
                  warps.put(warp, SpigotUtil.cell(essentials.getWarps().getWarp(warp)));
                } catch (WarpNotFoundException | InvalidWorldException e) {
                  // ignore
                }
              }
              return warps;
            })
            .build());
    User user = essentials.getUser(src.uuid());
    if (user.hasValidHomes()) {
      List<String> homes = user.getHomes();
      if (homes.size() == 1) {
        builder.item("home", SpigotUtil.cell(user.getHome(homes.get(0))));
      } else if (homes.size() > 1) {
        builder.subScope(Scope.builder("home")
            .items(user.getHomes()
                .stream()
                .collect(Collectors.toMap(name -> name, name ->
                    SpigotUtil.cell(user.getHome(name)))))
            .build());
      }
    }
    return builder.build();
  }

  private IEssentials essentials() {
    try {
      Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
      Class<IEssentials> essentialsClass = (Class<IEssentials>) Class.forName("com.earth2me.essentials.IEssentials");
      if (!(essentialsClass.isInstance(plugin))) {
        throw new RuntimeException("Essentials class could not be found");
      }
      return essentialsClass.cast(plugin);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Essentials class could not be found", e);
    }
  }
}
