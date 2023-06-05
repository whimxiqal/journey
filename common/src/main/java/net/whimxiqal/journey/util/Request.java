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
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.net.ssl.HttpsURLConnection;
import net.whimxiqal.journey.Journey;
import org.json.JSONObject;
import org.json.JSONTokener;

public final class Request {
  private Request() {
  }

  /**
   * Asynchronously call the Mojang API for the UUID of the player with the given name.
   * Player names may be changed, so this should only be called for requesting temporary information,
   * like for a user command to access a player by their name for an immediate one-time request.
   * @param player the player
   * @return the uuid
   */
  public static CompletableFuture<UUID> getPlayerUuidAsync(String player) {
    CompletableFuture<UUID> future = new CompletableFuture<>();
    Journey.get().proxy().schedulingManager().schedule(() -> {
      try {
        URL apiUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + player);
        URLConnection connection = apiUrl.openConnection();
        if (!(connection instanceof HttpsURLConnection httpsConnection)) {
          future.complete(null);
          return;
        }
        httpsConnection.setRequestMethod("GET");
        switch (httpsConnection.getResponseCode()) {
          case HttpsURLConnection.HTTP_OK:
            break;
          default:
            Journey.logger().warn("Mojang API request for player " + player + " resulted in response code: " + httpsConnection.getResponseCode());
            future.complete(null);
            return;
        }
        JSONObject obj = new JSONObject(new JSONTokener(new InputStreamReader(httpsConnection.getInputStream())));

        String hexString = obj.getString("id");
        byte[] uuidBytes = new byte[hexString.length() / 2];

        for (int i = 0; i < uuidBytes.length; i++) {
          int stringIndex = i * 2;
          uuidBytes[i] = (byte) Integer.parseInt(hexString.substring(stringIndex, stringIndex + 2), 16);
        }
        future.complete(UUIDUtil.bytesToUuid(uuidBytes));
      } catch (IOException e) {
        e.printStackTrace();
        future.complete(null);
      }
    }, true);
    return future;
  }
}
