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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.whimxiqal.journey.Journey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public final class Request {

  private static final int MAX_NEWER_VERSIONS_TO_SEND_CONSOLE = 5;
  private static final String MODRINTH_API_JOURNEY_PROJECT_VERSIONS_ENDPOINT = "https://api.modrinth.com/v2/project/journey/version";
  private static final String MINECRAFT_API_USER_PROFILE_API_ENDPOINT = "https://api.mojang.com/users/profiles/minecraft/";

  private Request() {
  }

  private static JSONTokener requestJson(String url) {
    try {
      URLConnection connection = new URL(url).openConnection();
      if (!(connection instanceof HttpURLConnection httpsConnection)) {
        return null;
      }
      httpsConnection.setRequestMethod("GET");
      if (httpsConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        Journey.logger().error("Request to " + url + " resulted in response code " + httpsConnection.getResponseCode()
            + ": " + httpsConnection.getResponseMessage());
        return null;
      }
      return new JSONTokener(new InputStreamReader(httpsConnection.getInputStream()));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Asynchronously call the Mojang API for the UUID of the player with the given name.
   * Player names may be changed, so this should only be called for requesting temporary information,
   * like for a user command to access a player by their name for an immediate one-time request.
   *
   * @param player the player
   * @return the uuid
   */
  public static PlayerResponse requestPlayerUuid(String player) {
    JSONTokener tokener = requestJson(MINECRAFT_API_USER_PROFILE_API_ENDPOINT + player);
    if (tokener == null) {
      return null;
    }
    JSONObject obj = new JSONObject(tokener);
    String hexString = obj.getString("id");
    byte[] uuidBytes = new byte[hexString.length() / 2];

    for (int i = 0; i < uuidBytes.length; i++) {
      int stringIndex = i * 2;
      uuidBytes[i] = (byte) Integer.parseInt(hexString.substring(stringIndex, stringIndex + 2), 16);
    }
    return new PlayerResponse(UUIDUtil.bytesToUuid(uuidBytes), obj.getString("name"));
  }

  private static Comparator<JourneyReleaseVersion> increasingReleaseVersionComparator() {
    return Comparator.<JourneyReleaseVersion, String>comparing(v -> v.versionTag, new ReleaseVersionComparator()).reversed();
  }

  /**
   * Get all the released versions that support the given loader, like Paper/Sponge
   *
   * @param loader the loader that the versions must support
   * @return the sorted list of versions, latest version first and oldest version last
   */
  public static List<JourneyReleaseVersion> requestReleasedVersions(String loader) {
    JSONTokener tokener = requestJson(MODRINTH_API_JOURNEY_PROJECT_VERSIONS_ENDPOINT);
    if (tokener == null) {
      return new LinkedList<>();
    }
    List<JourneyReleaseVersion> result = new LinkedList<>();
    JSONArray versions = new JSONArray(tokener);
    for (Object version : versions) {
      if (!(version instanceof JSONObject versionObj)) {
        throw new IllegalStateException("Unable to read version object from result from Modrinth API: " + version);
      }
      if (!versionObj.getString("status").equals("listed")) {
        // we only care about listed versions
        continue;
      }
      if (!versionObj.getString("version_type").equals("release")) {
        // we only care about releases
        continue;
      }
      boolean correctLoader = false;
      for (Object foundLoader : versionObj.getJSONArray("loaders")) {
        if (!(foundLoader instanceof String foundLoaderString)) {
          throw new IllegalStateException("Unable to read loader from result from Modrinth API: " + foundLoader);
        }
        if (foundLoaderString.equalsIgnoreCase(loader)) {
          correctLoader = true;
          break;
        }
      }
      if (correctLoader) {
        result.add(new JourneyReleaseVersion(versionObj.getString("version_number"), versionObj.getString("changelog")));
      }
    }
    result.sort(increasingReleaseVersionComparator());
    return result;
  }

  public static void evaluateVersionAge(String loader, String version) {
    Journey.get().proxy().schedulingManager().schedule(() -> {
      List<JourneyReleaseVersion> allReleasedVersions = requestReleasedVersions(loader);
      if (allReleasedVersions.isEmpty()) {
        // Should never be empty except in error scenarious
        Journey.logger().warn("Could not get latest versions from Modrinth. Please check to make sure you have the latest version.");
        return;
      }
      int index = Collections.binarySearch(allReleasedVersions, new JourneyReleaseVersion(version, null), increasingReleaseVersionComparator());
      if (index == 0) {
        // this is the latest official version, so we are fine
        return;
      }
      if (index >= 0) {
        // the version is an earlier official release version
        StringBuilder versionHistoryString = new StringBuilder();
        for (int i = 0; i < Math.min(index, MAX_NEWER_VERSIONS_TO_SEND_CONSOLE); i++) {
          JourneyReleaseVersion missingVersion = allReleasedVersions.get(i);
          versionHistoryString.append("\n%%% Version ")
              .append(missingVersion.versionTag)
              .append(" %%%\n")
              .append(missingVersion.changelog)
              .append("\n");
        }

        Journey.logger().warn("Your version of Journey is " + index
            + " version" + (index > 1 ? "s" : "")
            + " behind! Download the latest version at " + Links.DOWNLOAD_LINK
            + "\n" + versionHistoryString);
      } else {
        // Cannot find it as an official release
        Journey.logger().warn("You are running an unsupported version: " + version + ". Please download the latest official release (v" + allReleasedVersions.get(0).versionTag + ") at " + Links.DOWNLOAD_LINK);
      }
      Journey.logger().warn("To silence this message in the future, edit the extra.check-latest-version-on-startup setting in Journey's config.yml file.");
    }, true);
  }

  public static void checkForIntegrationPlugins(String loader, String gameVersion, Set<String> downloadedPlugins) {
    Journey.logger().info("Running checkForIntegrationPlugins: " + loader + ", " + gameVersion + ", " + downloadedPlugins);
    IntegrationPluginChecker checker = new IntegrationPluginChecker(loader, gameVersion, downloadedPlugins);
    Journey.get().proxy().schedulingManager().schedule(checker, true);
  }

  public static class IntegrationPluginChecker implements Runnable {
    private static final String JOURNEY_MODRINTH_PROJECT_ID = "nUFXQBU6";
    private static final String[] ALWAYS_ENDORSED_CREATORS = {"whimxiqal"};
    private static final String MODRINTH_API_PROJECT_ENDPOINT = "https://api.modrinth.com/v2/project/";
    private static final String MODRINTH_API_QUERY_ENDPOINT = "https://api.modrinth.com/v2/search";
    private static final String PLUGIN_CREATOR_ENDORSEMENT_LIST_DELIMITER_REGEX = "\\n\\s*-\\s*";
    private static final Pattern PLUGIN_CREATOR_ENDORSEMENT_PATTERN = Pattern.compile("Endorsed Creators:((?:" + PLUGIN_CREATOR_ENDORSEMENT_LIST_DELIMITER_REGEX + "\\S+)+)");
    private static final Pattern PLUGIN_INTEGRATION_MARKER = Pattern.compile("(?:[Ii]ntegrates|[Cc]onnects) Journey (?:to|with) (\\S+).?");
    private static final int MODRINTH_API_PAGE_SIZE = 100;
    private final CompletableFuture<Void> future = new CompletableFuture<>();

    private final String loader;
    private final String gameVersion;
    private final Set<String> downloadedPlugins;
    private final List<String> creatorsLeft = new LinkedList<>();
    private final List<String> unprocessedHitsQueue = new LinkedList<>();
    private final List<IntegrationPlugin> integrationPlugins = new LinkedList<>();
    private int currentTotalHits;
    private int currentHitsFound;

    public IntegrationPluginChecker(String loader, String gameVersion, Set<String> downloadedPlugins) {
      this.loader = loader;
      this.gameVersion = gameVersion;
      this.downloadedPlugins = downloadedPlugins.stream().map(plugin -> plugin.toLowerCase(Locale.ENGLISH)).collect(Collectors.toSet());
    }

    public CompletableFuture<Void> future() {
      return future;
    }

    protected RemoteJsonResponse requestModrinthJson(String url) {
      try {
        URLConnection connection = new URL(url).openConnection();
        if (!(connection instanceof HttpURLConnection httpsConnection)) {
          return null;
        }
        httpsConnection.setRequestMethod("GET");
        httpsConnection.setRequestProperty("User-Agent", "whimxiqal/journey/" + Journey.get().proxy().version() + "(whimxiqal@gmail.com)");
        if (httpsConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
          Journey.logger().error("Request to " + url + " resulted in response code " + httpsConnection.getResponseCode()
              + ": " + httpsConnection.getResponseMessage());
          return null;
        }
        int secondsToWait = 0;
        try {
          int rateLimitLimit = Integer.parseInt(httpsConnection.getHeaderField("X-Ratelimit-Limit"));
          int rateLimitRemaining = Integer.parseInt(httpsConnection.getHeaderField("X-Ratelimit-Remaining"));
          if (rateLimitRemaining < rateLimitLimit / 2) {
            secondsToWait = Integer.parseInt(httpsConnection.getHeaderField("X-Ratelimit-Reset"));
          }
        } catch (NumberFormatException e) {
          Journey.logger().error("Request to " + url + " returned rate limits that could not be parsed");
          return null;
        }
        return new RemoteJsonResponse(new JSONTokener(new InputStreamReader(httpsConnection.getInputStream())), secondsToWait);
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    }

    protected List<String> getEndorsedPluginCreators() {
      RemoteJsonResponse response = requestModrinthJson(MODRINTH_API_PROJECT_ENDPOINT + JOURNEY_MODRINTH_PROJECT_ID);

      List<String> out = new LinkedList<>(Arrays.asList(ALWAYS_ENDORSED_CREATORS));
      if (response == null) {
        return out;
      }
      JSONObject obj = new JSONObject(response.tokener);
      String body;
      try {
        body = obj.getString("body");
      } catch (JSONException e) {
        Journey.logger().error("Error parsing Journey description from Modrinth");
        e.printStackTrace();
        return out;
      }
      Matcher matcher = PLUGIN_CREATOR_ENDORSEMENT_PATTERN.matcher(body);
      if (!matcher.matches()) {
        return out;
      }
      String[] creators = matcher.group(1).split(PLUGIN_CREATOR_ENDORSEMENT_LIST_DELIMITER_REGEX);
      for (String creator : creators) {
        String trimmed = creator.trim();
        if (trimmed.isEmpty()) {
          continue;
        }
        out.add(trimmed);
      }
      return out;
    }

    protected boolean debug() {
      return false;
    }

    @Override
    public void run() {
      creatorsLeft.addAll(getEndorsedPluginCreators());
      resumeSafe();
    }

    private void resumeSafe() {
      try {
        resume();
      } catch (JSONException | ClassCastException e) {
        Journey.logger().error("Error trying to fetch integration plugins for Journey: " + e.getMessage());
        if (debug()) {
          // rethrow
          throw e;
        }
      }
    }

    private void resume() throws JSONException {
      for (Iterator<String> creatorsIt = creatorsLeft.iterator(); creatorsIt.hasNext(); ) {
        String creator = creatorsIt.next();
        String baseQueryUrl = MODRINTH_API_QUERY_ENDPOINT + "?" +
            "facets=[[%22author:" +  // %22 is "
            creator +
            "%22]]&index=newest" +  // %22 is "
            "&limit=" + MODRINTH_API_PAGE_SIZE;  // sort by newest for deterministic iterative results below
        do {
          if (currentHitsFound > currentTotalHits) {
            Journey.logger().error(String.format("Internal error attempting to fetch integration plugins from Modrinth: hits found (%d) > total hits (%d)", currentHitsFound, currentTotalHits));
            future.complete(null);
            return;
          }
          // Process any unprocessed hits
          boolean processedHit = !unprocessedHitsQueue.isEmpty();
          int secondsToWait = 0;
          for (Iterator<String> unprocessedHitsIt = unprocessedHitsQueue.iterator(); unprocessedHitsIt.hasNext(); unprocessedHitsIt.remove()) {
            if (secondsToWait > 0) {  // put this check to wait at the start so "continues" jump to here
              Journey.get().proxy().schedulingManager().schedule(this::resumeSafe, true, (secondsToWait + 1) * 20);
              return;
            }
            String unprocessedHit = unprocessedHitsIt.next();
            // Send request for hit
            RemoteJsonResponse response = requestModrinthJson(MODRINTH_API_PROJECT_ENDPOINT + unprocessedHit);
            if (response == null) {
              Journey.logger().error("Error fetching plugin details from Modrinth for project id: " + unprocessedHit);
              future.complete(null);
              return;
            }
            JSONObject jsonProject = new JSONObject(response.tokener);
            String slug = jsonProject.getString("slug");
            // slug on Modrinth is not == to project name in java plugin, but usually it should be
            if (downloadedPlugins.contains(slug.toLowerCase(Locale.ENGLISH))) {
              continue;
            }

            // Check body of plugin information to determine if it's an integration plugin
            String body = jsonProject.getString("body");
            Matcher matcher = PLUGIN_INTEGRATION_MARKER.matcher(body);
            if (!matcher.find()) {
              // not an integration plugin
              continue;
            }
            String downloadedPlugin = matcher.group(1);
            if (!downloadedPlugins.contains(downloadedPlugin.toLowerCase(Locale.ENGLISH))) {
              // We found a plugin that doesn't integrate with any plugins downloaded on this system
              continue;
            }
            // lets guarantee that this project has Journey and at least one other project as a required dependency.

            // Get the versions and check if there are any that match the loader, game version, and dependencies
            RemoteJsonResponse versionResponse = requestModrinthJson(MODRINTH_API_PROJECT_ENDPOINT + unprocessedHit + "/version");
            if (versionResponse == null) {
              future.complete(null);
              return;
            }
            secondsToWait = versionResponse.secondsToWait;
            JSONArray jsonVersionResponse = new JSONArray(versionResponse.tokener);
            IntegrationPluginVersion pluginVersion = null;
            for (Object versionObj : jsonVersionResponse) {
              JSONObject jsonVersion = (JSONObject) versionObj;
              JSONArray dependencies = jsonVersion.getJSONArray("dependencies");
              if (dependencies.length() < 2) {
                // Need at least 2 dependencies to count
                break;
              }
              boolean foundJourneyDependency = false;
              boolean foundAnotherDependency = false;
              for (Object dependencyObj : dependencies) {
                JSONObject dependency = (JSONObject) dependencyObj;
                if (dependency.getString("project_id").equals(JOURNEY_MODRINTH_PROJECT_ID)) {
                  foundJourneyDependency = true;
                  continue;
                }
                if (dependency.getString("dependency_type").equals("required")) {
                  foundAnotherDependency = true;
                }
              }
              if (!foundJourneyDependency || !foundAnotherDependency) {
                // We don't have the right dependencies for this to be a valid integration plugin
                continue;
              }

              // make sure loader is correct
              JSONArray projectLoaders = jsonProject.getJSONArray("loaders");
              boolean correctLoader = false;
              for (Object projectLoader : projectLoaders) {
                String projectLoaderString = (String) projectLoader;
                if (projectLoaderString.equalsIgnoreCase(loader)) {
                  correctLoader = true;
                  break;
                }
              }
              if (!correctLoader) {
                // This project might be marked as an integration plugin, but it does not support the given loader
                continue;
              }

              // make sure game version is correct
              JSONArray projectGameVersions = jsonProject.getJSONArray("game_versions");
              boolean correctGameVersion = false;
              for (Object projectGameVersion : projectGameVersions) {
                if (((String) projectGameVersion).equalsIgnoreCase(gameVersion)) {
                  correctGameVersion = true;
                  break;
                }
              }
              if (!correctGameVersion) {
                // This project might be marked as an integration plugin, but it does not support the game version
                continue;
              }

              String versionString = jsonVersion.getString("version_number");
              if (pluginVersion != null) {
                if (new ReleaseVersionComparator().compare(versionString, pluginVersion.versionNumber) <= 0) {
                  // this new version we found isn't a higher version number than a previously found matching version,
                  // so just skip
                  continue;
                }
              }
              pluginVersion = new IntegrationPluginVersion(versionString, jsonVersion.getInt("downloads"));

            }

            if (pluginVersion == null) {
              continue;
            }

            // this is an integration plugin! Get all the info from the JSON response and save
            String title = jsonProject.getString("title");
            String description = jsonProject.getString("description");
            integrationPlugins.add(new IntegrationPlugin(slug, title, downloadedPlugin, creator, description, pluginVersion));
          }
          if (processedHit && currentHitsFound == currentTotalHits) {
            // done processing for this creator, we've processed all the hits we can
            break;
          }

          // Done with unprocessed hits and we have more to query, so lets grab some more
          StringBuilder queryBuilder = new StringBuilder(baseQueryUrl);
          if (currentHitsFound > 0) {
            queryBuilder.append("&offset=").append(currentHitsFound);
          }
          RemoteJsonResponse response = requestModrinthJson(queryBuilder.toString());
          if (response == null) {
            future.complete(null);
            return;
          }
          JSONObject obj = new JSONObject(response.tokener);

          // Sanity check response
          int offset = obj.getInt("offset");
          if (offset != currentHitsFound) {
            Journey.logger().error("Incorrect response when fetching integration plugins from Modrinth: " +
                offset + " != " + currentHitsFound);
            future.complete(null);
            return;
          }
          int limit = obj.getInt("limit");
          if (limit != MODRINTH_API_PAGE_SIZE) {
            Journey.logger().error("Incorrect response when fetching integration plugins from Modrinth: " +
                limit + " != " + MODRINTH_API_PAGE_SIZE);
            future.complete(null);
            return;
          }
          currentTotalHits = obj.getInt("total_hits");

          // Parse hits
          JSONArray jsonHits = obj.getJSONArray("hits");

          for (Object hitObj : jsonHits) {
            // finally, add to unprocessed queue so that we can check if this is listed as an integration plugin next
            unprocessedHitsQueue.add(((JSONObject) hitObj).getString("project_id"));
          }
          currentHitsFound += jsonHits.length();

          // pause now if we're getting too close to the API limit
          if (response.secondsToWait > 0) {
            Journey.get().proxy().schedulingManager().schedule(this::resumeSafe, true, (response.secondsToWait + 1) * 20);
            return;
          }

        } while (!unprocessedHitsQueue.isEmpty() || currentHitsFound < currentTotalHits);
        creatorsIt.remove();
      }

      if (!integrationPlugins.isEmpty()) {
        // We're done grabbing all the data! Send the messages
        Journey.logger().warn("Some of your installed plugins may integrate with Journey if you download the appropriate extra plugins:");
        Journey.logger().warn("");
        for (IntegrationPlugin plugin : integrationPlugins) {
          Journey.logger().warn(String.format("\t- %s (v%s) by %s, %d downloads: %s (Journey <-> %s)",
              plugin.title, plugin.version.versionNumber, plugin.author, plugin.version.downloads, plugin.description, plugin.downloadedPlugin));
        }
        Journey.logger().warn("");
        Journey.logger().warn("To silence this message in the future, edit the extra.find-integrations-on-startup setting in Journey's config.yml file.");
      }
      future.complete(null);
    }

    record RemoteJsonResponse(JSONTokener tokener, int secondsToWait) {
    }

    private record IntegrationPluginVersion(String versionNumber, int downloads) {
    }

    private record IntegrationPlugin(String slug, String title, String downloadedPlugin, String author,
                                     String description, IntegrationPluginVersion version) {
    }

  }

  public record PlayerResponse(UUID uuid, String name) {
  }

  public record JourneyReleaseVersion(String versionTag, String changelog) {
  }

}
