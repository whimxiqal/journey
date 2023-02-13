package net.whimxiqal.journey.util;

public enum Permission {

  CANCEL("journey.cancel"),

  // Waypoints/Paths (Journey-to)
  PATH_GUI("journey.path.gui"),
  PATH_PERSONAL("journey.path.personal"),
  PATH_SERVER("journey.path.server"),
  PATH_DEATH("journey.path.death"),
  PATH_SURFACE("journey.path.surface"),
  PATH_PLAYER_ENTITY("journey.path.player.entity"),
  PATH_PLAYER_WAYPOINTS("journey.path.player.waypoints"),

  // Edit
  EDIT_PERSONAL("journey.edit.personal"),
  EDIT_PERSONAL_PUBLICITY("journey.edit.personal.publicity"),
  EDIT_SERVER("journey.edit.server"),

  // Admin
  ADMIN_DEBUG("journey.admin.debug"),
  ADMIN_CACHE("journey.admin.cache"),
  ADMIN_RELOAD("journey.admin.reload"),
  ADMIN_INFO("journey.admin.info"),

  // Flags
  FLAG_TIMEOUT("journey.flag.timeout"),
  FLAG_ANIMATE("journey.flag.animate"),
  FLAG_FLY("journey.flag.fly"),
  FLAG_DOOR("journey.flag.door"),
  FLAG_DIG("journey.flag.dig");

  private final String path;

  Permission(String path) {
    this.path = path;
  }

  public String path() {
    return path;
  }

}
