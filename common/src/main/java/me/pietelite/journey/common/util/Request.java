package me.pietelite.journey.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import me.pietelite.journey.common.Journey;
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
      future.complete(UUID.nameUUIDFromBytes(obj.getString("id").getBytes(StandardCharsets.UTF_8)));
    }, true);
    return future;
  }
}
