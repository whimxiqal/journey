/*
 * MIT License
 *
 * Copyright (c) whimxiqal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.whimxiqal.journey.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import net.whimxiqal.journey.JourneyTestHarness;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class RequestTest extends JourneyTestHarness {

  private static final String DISABLED_FOR_API_LIMITING_MESSAGE = "Tests should not be dependent on API rate limiting, so tests involving API calls are disabled but can be run manually";

  @Test
  @Disabled(DISABLED_FOR_API_LIMITING_MESSAGE)
  public void testGetPlayerUuid() {
    Request.PlayerResponse notchPlayer = Request.requestPlayerUuid("Notch");
    Assertions.assertNotNull(notchPlayer, "Request to get UUID failed");
    Assertions.assertEquals("069a79f4-44e9-4726-a5be-fca90e38aaf5", notchPlayer.uuid().toString(), "Request got the wrong UUID");
  }

  @Test
  @Disabled(DISABLED_FOR_API_LIMITING_MESSAGE)
  public void testGetVersions() {
    List<Request.JourneyReleaseVersion> versions = Request.requestReleasedVersions("paper");
    Assertions.assertEquals("1.0.0", versions.get(versions.size() - 1).versionTag(), "Version 1.0.0 was not the oldest version returned from Modrinth");
    boolean foundVersion = false; // found version 1.1.0
    for (Request.JourneyReleaseVersion version : versions) {
      if (version.versionTag().equals("1.1.0")) {
        foundVersion = true;
        break;
      }
    }
    Assertions.assertTrue(foundVersion, "Could not find version 1.1.0 in versions returned from Modrinth");
  }

  static class TestIntegrationPluginChecker extends Request.IntegrationPluginChecker {

    public TestIntegrationPluginChecker(String loader, String gameVersion, Set<String> downloadedPlugins) {
      super(loader, gameVersion, downloadedPlugins);
    }

    @Override
    protected Request.IntegrationPluginChecker.RemoteJsonResponse requestModrinthJson(String url) {
      System.out.println("Sending request: " + url);
      return super.requestModrinthJson(url);
    }

    @Override
    protected List<String> getEndorsedPluginCreators() {
      return List.of("whimxiqal");
    }

    @Override
    protected boolean debug() {
      return true;
    }
  }

  @Test
  @Disabled(DISABLED_FOR_API_LIMITING_MESSAGE)
  public void testIntegrationPluginMessage() throws ExecutionException, InterruptedException {
    Request.IntegrationPluginChecker checker = new TestIntegrationPluginChecker("paper", "1.20.1", Set.of("Essentials"));
    checker.run();
    checker.future().get();
  }

}