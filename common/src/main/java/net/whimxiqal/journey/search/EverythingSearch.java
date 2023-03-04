package net.whimxiqal.journey.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.search.event.StartSearchEvent;
import net.whimxiqal.journey.search.flag.Flags;
import net.whimxiqal.journey.util.SimpleTimer;

public class EverythingSearch extends SearchSession {

  private static final int MAX_CELL_COUNT = 10000;  // smaller than normal so we can get through these path trials pretty quick
  private static final int LOG_PERIOD_MS = 5000;
  private final LinkedList<PathTrial> pathTrials = new LinkedList<>();
  private double totalLengthToCalculate = 0;
  private double lengthCalculated = 0;
  private final SimpleTimer logTimer = new SimpleTimer();

  public EverythingSearch() {
    super(Journey.JOURNEY_CALLER, Caller.OTHER);
    flags.addFlag(Flags.TIMEOUT, -1);
    flags.addFlag(Flags.FLY, false);
    flags.addFlag(Flags.ANIMATE, 0);
    flags.addFlag(Flags.DIG, false);
    flags.addFlag(Flags.DOOR, true);
  }

  @Override
  protected void resumeSearch() {
    boolean shouldInit = false;
    synchronized (this) {
      if (state == ResultState.IDLE) {
        shouldInit = true;
        state = ResultState.RUNNING;
      }
    }
    if (shouldInit) {
      initSearch();
    }
    runSearchUnit();
  }

  private void initSearch() {
    Journey.logger().info("Performing preliminary caching to speed up search times...");
    super.timer.start();
    logTimer.start();
    Journey.get().dispatcher().dispatch(new StartSearchEvent(this));
    Journey.get().debugManager().broadcast(Formatter.debug("Started a search for caller ___, modes:___, tunnels:___",
            getCallerId(),
            modes().size(),
            tunnels().size()),
        getCallerId());

    final Set<Integer> allDomains = new HashSet<>();
    final Map<Integer, List<Tunnel>> tunnelsByOriginDomain = new HashMap<>();
    final Map<Integer, List<Tunnel>> tunnelsByDestinationDomain = new HashMap<>();

    for (Tunnel tunnel : this.tunnels) {
      allDomains.add(tunnel.origin().domain());
      allDomains.add(tunnel.destination().domain());
    }

    // Prepare tunnel maps
    for (Integer domain : allDomains) {
      tunnelsByOriginDomain.put(domain, new LinkedList<>());
      tunnelsByDestinationDomain.put(domain, new LinkedList<>());
    }

    // Fill tunnel maps
    for (Tunnel tunnel : this.tunnels) {
      tunnelsByOriginDomain.get(tunnel.origin().domain()).add(tunnel);
      tunnelsByDestinationDomain.get(tunnel.destination().domain()).add(tunnel);
    }

    // Collect path trials
    Set<ModeType> modeTypes = modes.stream().map(Mode::type).collect(Collectors.toSet());
    for (Integer domain : allDomains) {
      for (Tunnel pathTrialOriginTunnel : tunnelsByDestinationDomain.get(domain)) {
        for (Tunnel pathTrialDestinationTunnel : tunnelsByOriginDomain.get(domain)) {
          if (!Journey.get().dataManager()
              .pathRecordManager()
              .containsRecord(pathTrialOriginTunnel.destination(), pathTrialDestinationTunnel.origin(), modeTypes)) {
            PathTrial pathTrial = PathTrial.approximate(this, pathTrialOriginTunnel.destination(), pathTrialDestinationTunnel.origin(),
                modes, true);
            pathTrials.add(pathTrial);
            totalLengthToCalculate += pathTrial.getLength();
          }
        }
      }
    }
  }

  private void runSearchUnit() {
    if (pathTrials.isEmpty()) {
      Journey.logger().info("Caching complete!");
      markStopped();
      return;
    }
    if (logTimer.elapsed() > LOG_PERIOD_MS) {
      logTimer.start(); // restart timer
      Journey.logger().info("Caching path data... " + String.format("%.2f", lengthCalculated / totalLengthToCalculate * 100) + " % complete");
    }
    synchronized (this) {
      if (state.shouldStop()) {
        markStopped();
        return;
      }
    }

    PathTrial trial = pathTrials.pop();
    double preCalculatedLength = trial.getLength();
    trial.setMaxCellCount(MAX_CELL_COUNT);
    trial.attempt(false);  // ignore result
    lengthCalculated += preCalculatedLength;
  }

  @Override
  public void initialize() {
    Journey.get().proxy().platform().prepareSearchSession(this, getCallerId(), flags, true);
    Journey.get().netherManager().makeTunnels().forEach(this::registerTunnel);
    Journey.get().proxy().platform().onlinePlayer(getCallerId()).ifPresent(jPlayer ->
        Journey.get().tunnelManager().tunnels(jPlayer).forEach(this::registerTunnel));
  }

  @Override
  public Audience audience() {
    return Journey.get().proxy().audienceProvider().console();
  }

}
