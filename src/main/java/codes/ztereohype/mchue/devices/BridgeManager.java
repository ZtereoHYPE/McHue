package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.config.BridgeProperties;
import codes.ztereohype.mchue.gui.BridgeConnectionScreen;
import codes.ztereohype.mchue.util.NetworkUtil;
import net.shadew.json.IncorrectTypeException;
import net.shadew.json.Json;
import net.shadew.json.JsonNode;
import net.shadew.util.data.Pair;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BridgeManager {
    public static final Json JSON = Json.json();
    private static final int DEFAULT_POLL_INTERVAL = 2; //todo: this is a reminder to unhardcode as much as possible
    private static final int MAX_ATTEMPTS = 30;
    private static final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
    public static List<HueBridge> localBridges;
    private static ScheduledFuture<?> t;

    // Updates the localBridges list
    public static void scanBridges() {
        try {
            String response = new String(new URL("https://discovery.meethue.com/").openStream().readAllBytes());

            ArrayList<HueBridge> bridgeList = new ArrayList<>();
            for (JsonNode jsonBridge : JSON.parse(response)) {
                bridgeList.add(new HueBridge(jsonBridge.query("id").asString(), jsonBridge.query("internalipaddress")
                                                                                          .asString()));
            }

            localBridges = bridgeList;
        } catch (IncorrectTypeException | IOException e) {
            e.printStackTrace();
            localBridges = new ArrayList<>();
        }
    }

    public static void startInitialBridgeConnection(HueBridge bridge) {
        // Already complete bridge check
        if (bridge.isComplete()) return;

        if (bridge.getUsername() == null) {
            McHue.LOGGER.log(Level.INFO, "No id, generating it.");
            try {
                bridge.setUsername("mchue#" + InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                bridge.setUsername("mchue#unknown");
            }
        }

        if (bridge.getToken() == null) {
            McHue.LOGGER.log(Level.INFO, "No username, generating it.");

            String url = "http://" + bridge.getBridgeIp() + "/api";
            String data = "{\"devicetype\":\"" + bridge.getUsername() + "\"}";

            // only valid for 30 attempts
            UsernameCreator uc = new UsernameCreator(url, data, bridge);

            t = scheduler.scheduleAtFixedRate(uc, 0, DEFAULT_POLL_INTERVAL, TimeUnit.SECONDS);
        }
    }

    public static void cancelConnection() {
        t.cancel(true);
    }

    private static class UsernameCreator implements Runnable {
        private final String url;
        private final String data;
        private final HueBridge bridge;
        private final BridgeConnectionScreen bridgeConnectionScreen = McHue.settingsScreen.bridgeConnectionScreen;
        private int attempt = 1;

        protected UsernameCreator(String url, String data, HueBridge bridge) {
            this.url = url;
            this.data = data;
            this.bridge = bridge;
        }

        private static Pair<BridgeResponse, String> attemptUsernameCreation(String url, String data) {
            //todo: add more useful solutions to exceptions eg. "Are you connected to internet?"
            Optional<JsonNode> response = NetworkUtil.postJson(url, data);
            if (response.isEmpty()) return Pair.of(BridgeResponse.FAILURE, "Failed sending the request to the bridge");

            JsonNode parsedResponse = response.get().query("[0]");
            if (parsedResponse.has("success")) {
                return Pair.of(BridgeResponse.SUCCESS, parsedResponse.query("success.username").asString());

            } else if (parsedResponse.has("error") && parsedResponse.query("error.type").asInt() == 101) {
                return Pair.of(BridgeResponse.PRESS_BUTTON, "Please press the bridge button.");


            } else {
                McHue.LOGGER.log(Level.ERROR, "The bridge responded in an unknown way: " + parsedResponse);
                return Pair.of(BridgeResponse.FAILURE, "The bridge responded in an unknown way. Check logs for more info.");
            }
        }

        public void run() {
            Pair<BridgeResponse, String> usernameAttempt = attemptUsernameCreation(url, data);

            // unknown error
            if (usernameAttempt.first().equals(BridgeResponse.FAILURE)) {
                scheduler.shutdown();
                bridgeConnectionScreen.setConnectionUpdate(usernameAttempt.second());
                bridgeConnectionScreen.connectionComplete();
                t.cancel(false);
            }

            // success
            if (usernameAttempt.first().equals(BridgeResponse.SUCCESS)) {
                scheduler.shutdown();
                bridge.setToken(usernameAttempt.second());

                //save all info on config
                McHue.BRIDGE_DATA.setProperty(BridgeProperties.BRIDGE_ID, bridge.getBridgeId());
                McHue.BRIDGE_DATA.setProperty(BridgeProperties.BRIDGE_IP, bridge.getBridgeIp());
                McHue.BRIDGE_DATA.setProperty(BridgeProperties.DEVICE_INDENTIFIER, bridge.getUsername());
                McHue.BRIDGE_DATA.setProperty(BridgeProperties.USERNAME, bridge.getToken());

                if (bridgeConnectionScreen != null) {
                    bridgeConnectionScreen.setConnectionUpdate("Connection completed with Success!");
                    bridgeConnectionScreen.connectionComplete();
                }
            }

            if (attempt >= MAX_ATTEMPTS) {
                t.cancel(true);
                if (bridgeConnectionScreen != null) {
                    bridgeConnectionScreen.setConnectionUpdate("The button was not pressed in 60 seconds.");
                    bridgeConnectionScreen.connectionComplete();
                }
            }
            attempt++;
        }
    }
}
