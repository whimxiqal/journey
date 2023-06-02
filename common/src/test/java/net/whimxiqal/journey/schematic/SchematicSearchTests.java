package net.whimxiqal.journey.schematic;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.PlatformsRegisteredEvent;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.InternalJourneyPlayer;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.TestProxy;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.navigation.mode.JumpMode;
import net.whimxiqal.journey.navigation.mode.WalkMode;
import net.whimxiqal.journey.platform.TestPlatformProxy;
import net.whimxiqal.journey.search.DestinationPathTrial;
import net.whimxiqal.journey.search.ResultState;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.util.CommonLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class SchematicSearchTests {

  public static final boolean DEBUG = false;
  ////// BENCHMARK PARAMS
  private static final String SCHEMATIC_FILE = "overworld1.schem";
  private static final Cell PATH_START = new Cell(-15, 64, -56, 0);
  private static final Cell PATH_END = new Cell(-117, 93, -203, 0);
  public static InternalJourneyPlayer PLAYER = new SchematicPlayer();
  private static boolean mockedBlockRegistry = false;
  ///// END BENCHMARK PARAMS

  static void tryMockAllBlockTypes() {
    if (mockedBlockRegistry) {
      return;
    }
    MockSettings settings = Mockito.withSettings()
        .stubOnly()
        .defaultAnswer(Answers.RETURNS_SMART_NULLS);
    for (String id : AllBlockTypes.IDS) {
      BlockType blockType = Mockito.mock(BlockType.class, settings);
      BlockState blockState = Mockito.mock(BlockState.class, settings);
      BaseBlock baseBlock = Mockito.mock(BaseBlock.class, settings);

      Mockito.when(blockType.getId()).thenReturn(id);
      Mockito.when(blockType.getDefaultState()).thenReturn(blockState);
      Mockito.when(blockState.getBlockType()).thenReturn(blockType);
      Mockito.when(blockState.toBaseBlock()).thenReturn(baseBlock);
      Mockito.when(blockState.toBaseBlock(Mockito.any())).thenReturn(baseBlock);
      Mockito.when(baseBlock.getBlockType()).thenReturn(blockType);
      Mockito.when(baseBlock.toBaseBlock()).thenReturn(baseBlock);
      Mockito.when(baseBlock.toImmutableState()).thenReturn(blockState);

      BlockType.REGISTRY.register(id, blockType);
    }
    mockedBlockRegistry = true;
  }

  @Test
  @Disabled()
  void benchmark() throws RunnerException {
    Options opt = new OptionsBuilder()
        // Specify which benchmarks to run.
        // You can be more specific if you'd like to run only one benchmark per test.
        .include(this.getClass().getName() + ".*")
        // Set the following options as needed
        .mode(Mode.AverageTime)
        .timeUnit(TimeUnit.MILLISECONDS)
        .warmupTime(TimeValue.seconds(1))
        .warmupIterations(1)
        .measurementTime(TimeValue.seconds(5))
        .measurementIterations(1)
        .threads(1)
        .forks(1)
        .shouldFailOnError(true)
        .shouldDoGC(true)
//        .param("weight", DoubleStream.iterate(1, cur -> cur + 0.1).limit(40).mapToObj(String::valueOf).toArray(String[]::new))
        .build();

    new Runner(opt).run();
  }

  @Benchmark
  public void benchmark(BenchmarkState state) {
    /// USE PARAMETERS

    /// USE PARAMETERS END
    SearchSession session = new DummySearchSession();
    DestinationPathTrial pathTrial = new DestinationPathTrial(session,
        PATH_START,
        PATH_END,
        List.of(new WalkMode(), new JumpMode()),
        0, null, ResultState.IDLE, false, false);

    while (!pathTrial.run()) {
      // do nothing, just wait utnil path trial is complete
    }

    Journey.logger().flush();
    Assertions.assertEquals(ResultState.STOPPED_SUCCESSFUL, pathTrial.getState());

    state.pathLength = pathTrial.getPath().getCost();
    state.totalVisited = pathTrial.getTotalVisitedCells();
    state.cycles = pathTrial.getCycles();
  }

  @AuxCounters(AuxCounters.Type.EVENTS)
  @State(Scope.Thread)
  public static class BenchmarkState {
    private final SchematicLoader loader = new SchematicLoader();

    /// Aux Counters Start (must be public and primitives)
    public double pathLength;
    public int totalVisited;
    public int cycles;
    /// Aux Counters End

    /// Parameters (set to private)
//    @Param("1")
//    private double weight = 1;
    /// Parameters End

    @Setup(Level.Iteration)
    public void setUp() {
      tryMockAllBlockTypes();
      WorldEdit.getInstance().getPlatformManager().register(new JourneyPlatform());
      WorldEdit.getInstance().getEventBus().post(new PlatformsRegisteredEvent());

      Journey.create();
      TestProxy proxy = new TestProxy(new SchematicPlatformProxy(loader));
      Journey.get().registerProxy(proxy);

      if (DEBUG) {
        Journey.logger().setLevel(CommonLogger.LogLevel.DEBUG);
      }
      if (!Journey.get().init()) {
        Assertions.fail("Journey initialization failed");
      }

      Journey.get().tunnelManager().register(player -> TestPlatformProxy.tunnels);
      Assertions.assertTrue(loader.load(SCHEMATIC_FILE), "Schematic load failed");

      Settings.MAX_PATH_BLOCK_COUNT.setValue(100000);

    }

    @TearDown(Level.Iteration)
    public void tearDown() {
      Journey.get().shutdown();
      Journey.remove();
    }

  }
}
