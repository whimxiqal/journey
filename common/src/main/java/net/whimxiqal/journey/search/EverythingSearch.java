package net.whimxiqal.journey.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.search.event.StartSearchEvent;
import net.whimxiqal.journey.search.event.StopSearchEvent;
import net.whimxiqal.journey.search.flag.Flags;
import net.whimxiqal.journey.util.SimpleTimer;

public class EverythingSearch extends SearchSession {

  private static final int LOG_PERIOD_MS = 1000;  // 5 seconds
  private final LinkedList<PathTrial> pathTrials = new LinkedList<>();
  private final SimpleTimer logTimer = new SimpleTimer();
  private final AtomicReference<Double> totalLengthToCalculate = new AtomicReference<>(0.0);
  private final AtomicReference<Double> lengthCalculated = new AtomicReference<>(0.0);
  private int pathTrialsCompleted = 0;

  public EverythingSearch(UUID caller, Caller callerType) {
    super(caller, callerType);
    flags.addFlag(Flags.TIMEOUT, -1);
    flags.addFlag(Flags.FLY, false);
    flags.addFlag(Flags.ANIMATE, 0);
    flags.addFlag(Flags.DIG, false);
    flags.addFlag(Flags.DOOR, true);
  }

  @Override
  public void asyncSearch() {
    synchronized (this) {
      if (state == ResultState.IDLE) {
        state = ResultState.RUNNING;
      } else {
        throw new RuntimeException();  // programmer error
      }
    }
    initSearch();
    runSearches();
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
            totalLengthToCalculate.set(totalLengthToCalculate.get() + pathTrial.getLength());
          }
        }
      }
    }
  }

  private void runSearches() {
    if (pathTrials.isEmpty()) {
      Journey.logger().info("Caching paths: No paths to cache!");
      state = ResultState.STOPPED_SUCCESSFUL;
      future.complete(state);
      Journey.get().dispatcher().dispatch(new StopSearchEvent(this));
      return;
    }

    Journey.logger().info("Caching paths: 0.00 % complete");
    for (PathTrial pathTrial : pathTrials) {
      double preCalculatedLength = pathTrial.getLength();

      if (pathTrial.isFromCache()) {
        // Already cached, let's not do this again unless we manually clear the cache
        onPathTrialComplete(preCalculatedLength);
      } else {
        Journey.get().workManager().schedule(pathTrial);
        pathTrial.future().thenAccept(result -> onPathTrialComplete(preCalculatedLength));
      }
    }
  }

  private void onPathTrialComplete(double preCalculatedLength) {
    synchronized (this) {
      double length = lengthCalculated.accumulateAndGet(preCalculatedLength, Double::sum);

      if (logTimer.elapsed() > LOG_PERIOD_MS) {
        logTimer.start(); // restart timer
        Journey.logger().info("Caching paths: " + String.format("%.2f", length / Math.max(totalLengthToCalculate.get(), 1) * 100) + " % complete");
      }

      pathTrialsCompleted++;
      if (pathTrialsCompleted == pathTrials.size()) {
        Journey.logger().info("Caching paths: complete!");
        state = state.stoppedResult();
        future.complete(state);
        Journey.get().dispatcher().dispatch(new StopSearchEvent(this));
      }
    }
  }

  @Override
  public void initialize() {
    SearchSession.registerPlayerModes(this, UUID.randomUUID(), flags);
    Journey.get().tunnelManager().tunnels(null).forEach(this::registerTunnel);
  }

  @Override
  public Audience audience() {
    return Journey.get().proxy().audienceProvider().player(getCallerId());
  }

}
