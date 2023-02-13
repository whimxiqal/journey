package net.whimxiqal.journey;

public final class JourneyApiSupplier {

  private JourneyApiSupplier() {
  }

  public static void set(JourneyApi api) {
    JourneyApiProvider.provide(api);
  }

}
