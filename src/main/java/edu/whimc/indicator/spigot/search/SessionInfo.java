package edu.whimc.indicator.spigot.search;

public class SessionInfo {

  private final PlayerSearchSession session;
  /**
   * True if the algorithm has found a path.
   */
  private boolean solved = false;
  private int successNotificationTaskId = 0;
  /**
   * True if the player has been presented with a solution (itinerary -> journey).
   */
  private boolean solutionPresented = false;

  public SessionInfo(PlayerSearchSession playerSearchSession) {
    this.session = playerSearchSession;
  }

  public void setSolved(boolean solved) {
    this.solved = solved;
  }

  public boolean wasSolved() {
    return solved;
  }

  public void setSuccessNotificationTaskId(int successNotificationTaskId) {
    this.successNotificationTaskId = successNotificationTaskId;
  }

  public int getSuccessNotificationTaskId() {
    return successNotificationTaskId;
  }

  public void setSolutionPresented(boolean solutionPresented) {
    this.solutionPresented = solutionPresented;
  }

  public boolean wasSolutionPresented() {
    return solutionPresented;
  }

//  public void foundNewOptimalPath(Itinerary<LocationCell, World> itinerary) {
//
//    Player player = Bukkit.getPlayer(session.getCallerId());
//    if (player == null) {
//      return;
//    }
//
//    if (solutionPresented) {
//      PlayerJourney journey = Indicator.getInstance().getSearchManager().getPlayerJourney(player.getUniqueId());
//      if (journey != null) {
//        journey.setProspectiveItinerary(itinerary);
//        player.spigot().sendMessage(Format.info("A faster itinerary to your destination was found from your original location"));
//        player.spigot().sendMessage(Format.chain(Format.info("Run "),
//            Format.command("/trail accept", "Accept an incoming trail request"),
//            Format.textOf(" to accept")));
//        return;
//      }
//    }
//
//    if (solved) {
//      Bukkit.getScheduler().cancelTask(successNotificationTaskId);
//    }
//    solved = true;
//
//    // Create a journey that is completed when the player reaches within 3 blocks of the endpoint
//    PlayerJourney journey = new PlayerJourney(player.getUniqueId(), session, itinerary);
//    journey.illuminateTrail();
//
//    // Save the journey
//    Indicator.getInstance().getSearchManager().putPlayerJourney(player.getUniqueId(), journey);
//
//    // Set up a success notification that will be cancelled if a better one is found in some amount of time
//    successNotificationTaskId = Bukkit.getScheduler()
//        .runTaskLater(Indicator.getInstance(),
//            () -> {
//              player.spigot().sendMessage(Format.success("Showing a itinerary to your destination"));
//              solutionPresented = true;
//            },
//            20 /* ticks per second */ * 2 /* seconds */)
//        .getTaskId();
//  }
//
//  public void startTrailSearch(LocationCell origin, LocationCell destination) {
//    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.PREFIX + Format.WARN + "Began" + Format.DEBUG + " a trail search: ");
//    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(
//        origin.toString()
//            + " -> "));
//    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(destination.toString()));
//  }
//
//  public void completeTrailSearch(LocationCell origin, LocationCell destination, double distance) {
//    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.PREFIX + Format.SUCCESS + "Finished" + Format.DEBUG + " a trail search: ");
//    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(
//        origin.toString()
//            + " -> "));
//    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(destination.toString()));
//    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug("Length: " + (distance > 1000000 ? "Inf" : Math.round(distance))));
//  }
//
//  public void memoryCapacityReached(LocationCell origin, LocationCell destination) {
//    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug("Ran out of allocated memory for a local trail search: "));
//    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(
//        origin.toString()
//            + " -> "));
//    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(destination.toString()));
//  }

}
