package net.whimxiqal.journey.bukkit.integration;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.WarpNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.ess3.api.InvalidWorldException;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import net.whimxiqal.journey.common.data.integration.Integrator;
import net.whimxiqal.journey.common.data.integration.Scope;
import net.whimxiqal.journey.common.data.integration.ScopeBuilder;
import net.whimxiqal.journey.common.message.Formatter;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.mantle.common.CommandSource;
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
        .name(Component.text("Essentials"))
        .description(Formatter.accent("Locations from the ___ plugin", "Essentials"))
        .addSubScope(Scope.builder("warp")
            .name(Component.text("Warps"))
            .addItems(() -> {
              Map<String, Cell> warps = new HashMap<>();
              for (String warp : essentials.getWarps().getList()) {
                try {
                  warps.put(warp, BukkitUtil.cell(essentials.getWarps().getWarp(warp)));
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
        builder.addItem("home", BukkitUtil.cell(user.getHome(homes.get(0))));
      } else if (homes.size() > 1) {
        builder.addSubScope(Scope.builder("home")
            .name(Component.text("Homes"))
            .description(Formatter.accent("Your homes saved in ___", "Essentials"))
            .addItems(user.getHomes()
                .stream()
                .collect(Collectors.toMap(name -> name, name ->
                    BukkitUtil.cell(user.getHome(name)))))
            .build());
      }
    }
    return builder.build();
  }

  private IEssentials essentials() {
    Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
    if (!(plugin instanceof IEssentials)) {
      throw new RuntimeException("Essentials class could not be found");
    }
    return (IEssentials) plugin;
  }
}
