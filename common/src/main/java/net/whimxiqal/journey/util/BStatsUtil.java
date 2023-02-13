package net.whimxiqal.journey.util;

import java.util.function.Consumer;
import net.whimxiqal.journey.Journey;
import org.bstats.charts.CustomChart;
import org.bstats.charts.SingleLineChart;

public class BStatsUtil {

  public static final int BSTATS_ID = 17665;

  private BStatsUtil() {
  }

  public static void register(Consumer<CustomChart> chartConsumer) {
    chartConsumer.accept(new SingleLineChart("Number of Searches per Hour", () -> Journey.get().statsManager().searches()));
    chartConsumer.accept(new SingleLineChart("Blocks Travelled per Hour", () -> Journey.get().statsManager().blocksTravelled()));
  }

}
