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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.json.Json;
import javax.json.JsonObject;
import net.whimxiqal.journey.Journey;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public final class Request {
  private Request() {
  }

  public static CompletableFuture<UUID> getPlayerUuid(String player) {
    CompletableFuture<UUID> future = new CompletableFuture<>();
    Journey.get().proxy().schedulingManager().schedule(() -> {
      HttpClient client = HttpClientBuilder.create().build();
      HttpGet request = new HttpGet("https://api.mojang.com/users/profiles/minecraft/" + player);
      request.addHeader("accept", "application/json");
      HttpResponse response;
      InputStream content;
      try {
        response = client.execute(request);
        content = response.getEntity().getContent();
      } catch (IOException e) {
        e.printStackTrace();
        future.complete(null);
        return;
      }
      JsonObject obj = Json.createReader(content).readObject();
      future.complete(UUIDUtil.bytesToUuid(obj.getString("id").getBytes(StandardCharsets.UTF_8)));
    }, true);
    return future;
  }
}
