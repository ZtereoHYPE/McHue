package codes.ztereohype.mchue.network;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.shadew.json.Json;
import net.shadew.json.JsonNode;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class NetworkUtil {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Json JSON = Json.json();
    private static final DatagramSocket datagramSocket;
    private static final SSLContext sslCtx;

    static {
        try {
            sslCtx = SSLContext.getInstance("DTLS");
//            sslCtx.init();

            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(10 * 1000);  // in millis, match Hue Bridge timeout
        } catch (SocketException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean putJson(String endpoint, String json, ObjectArrayList<String> extraHeaders) {
        extraHeaders.add("Content-Type");
        extraHeaders.add("application/json");

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(endpoint))
                                         .headers(extraHeaders.toArray(String[]::new))
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

    public static boolean putJson(String endpoint, String json) {
        return putJson(endpoint, json, new ObjectArrayList<>());
    }

    public static Optional<JsonNode> postJson(String endpoint, String json, ObjectArrayList<String> extraHeaders) {
        extraHeaders.add("Content-Type");
        extraHeaders.add("application/json");

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(endpoint))
                                         .headers(extraHeaders.toArray(String[]::new))
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

    public static Optional<JsonNode> postJson(String endpoint, String json) {
        return postJson(endpoint, json, new ObjectArrayList<>());
    }

    public static Optional<JsonNode> getJson(String endpoint, ObjectArrayList<String> extraHeaders) {
        extraHeaders.add("Content-Type");
        extraHeaders.add("application/json");

        var request = HttpRequest.newBuilder()
                                 .uri(URI.create(endpoint))
                                 .headers(extraHeaders.toArray(String[]::new))
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

    public static Optional<JsonNode> getJson(String endpoint) {
        return getJson(endpoint, new ObjectArrayList<>());
    }

//    public static boolean streamDatagramPacket(byte[] data, String host, int port) {
//        try {
//            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
//            datagramSocket.send(datagramPacket);
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static void runTest() {
//        var DTLSOD = new DTLSOverDatagram();
//
////        var sslEngine = new SSL
//
////        DTLSOD.handshake();
//    }
}
