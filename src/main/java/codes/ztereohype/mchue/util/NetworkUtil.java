package codes.ztereohype.mchue.util;

import net.shadew.json.Json;
import net.shadew.json.JsonNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class NetworkUtil {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Json JSON = Json.json();

    public static boolean putJson(String endpoint, String json) {
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(endpoint))
                                         .header("Content-Type", "application/json")
                                         .PUT(HttpRequest.BodyPublishers.ofString(json))
                                         .build();

        try {
            var req = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            //todo handle errors better from the body lol
//            System.out.println(req.statusCode() + " " + req.body());
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            //todo: in everything, return more detailed errors with an enum
            return false;
        }
    }

    public static Optional<JsonNode> postJson(String endpoint, String json) {
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(endpoint))
                                         .header("Content-Type", "application/json")
                                         .POST(HttpRequest.BodyPublishers.ofString(json))
                                         .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            //todo: be less cringe (accept other benign codes? handle this better overall)
            if (response.statusCode() != 200) return Optional.empty();

            return Optional.of(JSON.parse(response.body()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<JsonNode> getJson(String endpoint) {
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(endpoint))
                                         .header("Content-Type", "application/json")
                                         .GET()
                                         .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            //todo: be less cringe (accept other benign codes? handle... yeah)
            if (response.statusCode() != 200) return Optional.empty();

            return Optional.of(JSON.parse(response.body()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
