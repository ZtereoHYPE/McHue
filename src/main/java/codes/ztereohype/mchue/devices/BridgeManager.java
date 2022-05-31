package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.config.BridgeProperties;
import codes.ztereohype.mchue.devices.interfaces.BridgeResponse;
import codes.ztereohype.mchue.gui.screens.BridgeConnectionScreen;
import codes.ztereohype.mchue.util.NetworkUtil;
import it.unimi.dsi.fastutil.Pair;
import net.shadew.json.IncorrectTypeException;
import net.shadew.json.Json;
import net.shadew.json.JsonNode;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final int DEFAULT_POLL_INTERVAL = 1; //todo: this is a reminder to unhardcode as much as possible
    private static final int MAX_ATTEMPTS = 60;
    private static final Logger LOGGER = LogManager.getLogger("BridgeManager");
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
            String data = "{\"devicetype\":\"" + bridge.getUsername() + "\", \"generateclientkey\":true}";

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

        private Pair<BridgeResponse, String> attemptUsernameCreation(String url, String data) {
            //todo: add more useful solutions to exceptions eg. "Are you connected to internet?"
            Optional<JsonNode> response = NetworkUtil.postJson(url, data);
            if (response.isEmpty()) return Pair.of(BridgeResponse.FAILURE, "Failed sending the request to the bridge");

            JsonNode parsedResponse = response.get().query("[0]");
            if (parsedResponse.has("success")) {
                //todo: find a better way than the regex splitting
                return Pair.of(BridgeResponse.SUCCESS, parsedResponse.query("success.username").asString() + ";" + parsedResponse.query("success.clientkey").asString());

            } else if (parsedResponse.has("error") && parsedResponse.query("error.type").asInt() == 101) {
                return Pair.of(BridgeResponse.PRESS_BUTTON, String.valueOf(DEFAULT_POLL_INTERVAL * (MAX_ATTEMPTS - attempt)));

            } else {
                LOGGER.log(Level.ERROR, "The bridge responded in an unknown way: " + parsedResponse);
                return Pair.of(BridgeResponse.FAILURE, "The bridge responded in an unknown way. Check logs for more info.");
            }
        }

        public void run() {
            Pair<BridgeResponse, String> usernameAttempt = attemptUsernameCreation(url, data);
            //todo: is this assertion correct?
            assert bridgeConnectionScreen != null;

            if (attempt >= MAX_ATTEMPTS) {
                bridgeConnectionScreen.setSubtitle("The button was not pressed in 60 seconds.");
                bridgeConnectionScreen.setCountdown("You can press Try Again to... try again.");

                bridgeConnectionScreen.connectionComplete();
                t.cancel(true);

            } else switch (usernameAttempt.first()) {
                case FAILURE -> {
                    bridgeConnectionScreen.setSubtitle(usernameAttempt.second());
                    bridgeConnectionScreen.setCountdown("");
                    bridgeConnectionScreen.connectionComplete();

                    scheduler.shutdown();
                    t.cancel(false);
                }
                case SUCCESS -> {
                    scheduler.shutdown();
                    bridge.setToken(usernameAttempt.second().split(";")[0]);
                    bridge.setClientKey(usernameAttempt.second().split(";")[1]);

                    McHue.BRIDGE_DATA.setProperty(BridgeProperties.BRIDGE_ID, bridge.getBridgeId());
                    McHue.BRIDGE_DATA.setProperty(BridgeProperties.BRIDGE_IP, bridge.getBridgeIp());
                    McHue.BRIDGE_DATA.setProperty(BridgeProperties.DEVICE_INDENTIFIER, bridge.getUsername());
                    McHue.BRIDGE_DATA.setProperty(BridgeProperties.USERNAME, bridge.getToken());
                    McHue.BRIDGE_DATA.setProperty(BridgeProperties.CLIENT_KEY, bridge.getClientKey());

                    bridgeConnectionScreen.setSubtitle("Connection completed with Success!");
                    bridgeConnectionScreen.setCountdown("You may go back to the previous screen.");
                    bridgeConnectionScreen.connectionComplete();
                }
                case PRESS_BUTTON -> bridgeConnectionScreen.setCountdown(usernameAttempt.second() + "s remaining...");
            }

            attempt++;
        }
    }
}
