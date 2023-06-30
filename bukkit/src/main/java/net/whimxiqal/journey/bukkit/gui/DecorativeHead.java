package net.whimxiqal.journey.bukkit.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class DecorativeHead {

  public static DecorativeHead PURPLE_CHEST = new DecorativeHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y2NmY3ZjAzMTI1Y2Y1NDczMzY5NmYzNjMyZjBkOWU2NDcwYmFhYjg0OTg0N2VhNWVhMmQ3OTE1NmFkMGY0MCJ9fX0=");
  public static DecorativeHead GRAY_CHEST = new DecorativeHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFhOTEyZTMzMmZjMDAxMGJlYmQwZjkzYTE0ZDhlM2VhNjVkMTMwMTEwMGNlYTNmYzVhZTcxOTkwZDk4NTgwNyJ9fX0=");
  public static DecorativeHead CYAN_LEFT_ARROW = new DecorativeHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTJiOGUzZWFlYTU1OGY4NmFlYmEzMjI5NjlkNGVlYjZiOTY5NDM0ZjVhZDc5MjY2ZDVkOTY4YjI4ZDkxOTJlIn19fQ==");
  public static DecorativeHead HOUSE = new DecorativeHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGYxN2E2YTlhZmFhN2IwN2U0MjE4ZmU1NTVmMjgyM2IwMjg0Y2Q2OWI0OWI2OWI5N2ZhZTY3ZWIyOTc2M2IifX19");

  private final String textureData;

  private DecorativeHead(String textureData) {
    this.textureData = textureData;
  }

  public ItemStack toItemStack() {
    ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
    ItemMeta meta = itemStack.getItemMeta();
    if (!(meta instanceof SkullMeta skullMeta)) {
      throw new RuntimeException();
    }
    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
    profile.setProperty(new ProfileProperty("textures", textureData));
    skullMeta.setPlayerProfile(profile);
    itemStack.setItemMeta(meta);
    return itemStack;
  }
}
