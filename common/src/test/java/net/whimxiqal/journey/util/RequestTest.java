package net.whimxiqal.journey.util;

import java.util.concurrent.ExecutionException;
import net.whimxiqal.journey.JourneyTestHarness;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RequestTest extends JourneyTestHarness {

  @Test
  public void testGetPlayerUuid() throws ExecutionException, InterruptedException {
    Assertions.assertEquals("069a79f4-44e9-4726-a5be-fca90e38aaf5", Request.getPlayerUuidAsync("Notch").get().toString());
  }

}