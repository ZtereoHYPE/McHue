package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.config.BridgeProperties;
import codes.ztereohype.mchue.devices.interfaces.BridgeConnectionUpdate;
import codes.ztereohype.mchue.devices.interfaces.BridgeResponse;
import codes.ztereohype.mchue.util.NetworkUtil;
import net.shadew.json.IncorrectTypeException;
import net.shadew.json.JsonNode;
import net.shadew.json.JsonPath;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.xbill.mDNS.Lookup;
import org.xbill.mDNS.ServiceInstance;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BridgeManager {
    private static final Logger LOGGER = LogManager.getLogger("BridgeManager");
    private static final List<HueBridge> bridgeCache = new ArrayList<>();

    private static final JsonPath ID_PATH = JsonPath.parse("id");
    private static final JsonPath IP_PATH = JsonPath.parse("internalipaddress");

    protected final int DEFAULT_POLL_INTERVAL = 1; //todo: this is a reminder to unhardcode as much as possible
    protected final int MAX_ATTEMPTS = 60;

    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
    private ScheduledFuture<?> t;

    public BridgeManager() {
    }

    public static List<HueBridge> getLocalBridges(boolean useCache) {
        if (useCache) return bridgeCache;

        bridgeCache.clear();
        // Attempt 1: using mDNS discovery
        try {
            Lookup lookup = new Lookup("_hue._tcp");
            ServiceInstance[] services = lookup.lookupServices();
            for (ServiceInstance service : services) {
                String ip = Arrays.stream(service.getAddresses()).filter(a -> a instanceof Inet4Address).findFirst()
                                  .get().getHostAddress();
                String id = (String) service.getTextAttributes().get("bridgeid");

                // sometimes we get "ghost" copies of the bridges without attributes so better make sure
                if (ip == null || id == null) continue;

                bridgeCache.add(new HueBridge(id, ip));
            }
        } catch (IOException e) {
            McHue.LOGGER.error(e);
        }

        if (!bridgeCache.isEmpty()) {
            return bridgeCache;
        }

        // Attempt 2: use the discovery api.
        try {
            McHue.LOGGER.info("mDNS failed, falling back to discovery api");

            Optional<JsonNode> response = NetworkUtil.getJson("https://discovery.meethue.com/");

            //todo: better error management
            if (response.isEmpty()) return bridgeCache;

            for (JsonNode jsonBridge : response.get()) {
                bridgeCache.add(new HueBridge(jsonBridge.query(ID_PATH).asString(), jsonBridge.query(IP_PATH)
                                                                                              .asString()));
            }
        } catch (IncorrectTypeException e) {
            e.printStackTrace();
        }
        return bridgeCache;
    }

    //todo: remake this from scratch to try and read the cnofig first and call when mchue launches instead of doing on the mcgue calss
    //returns true if the bridge is already completed after the execution of it
    public boolean completeBridge(HueBridge bridge) {
        return completeBridge(bridge, null);
    }

    public boolean completeBridge(HueBridge bridge, @Nullable Consumer<BridgeConnectionUpdate> updateMethod) {
        if (bridge.isComplete()) return true;

        boolean validSavedBridge = !(Objects.equals(McHue.BRIDGE_DATA.getProperty(BridgeProperties.BRIDGE_IP), "null")
                || Objects.equals(McHue.BRIDGE_DATA.getProperty(BridgeProperties.BRIDGE_ID), "null")
                || Objects.equals(McHue.BRIDGE_DATA.getProperty(BridgeProperties.USERNAME), "null")
                || Objects.equals(McHue.BRIDGE_DATA.getProperty(BridgeProperties.DEVICE_INDENTIFIER), "null")
                || Objects.equals(McHue.BRIDGE_DATA.getProperty(BridgeProperties.CLIENT_KEY), "null"));

        boolean bridgeIsInConfig = validSavedBridge && McHue.BRIDGE_DATA.getProperty(BridgeProperties.BRIDGE_ID)
                                                                        .equals(bridge.getBridgeId());

        DEVICE_ID_CHECK:
        if (bridge.getDeviceId() == null) {
            if (bridgeIsInConfig) {
                bridge.setDeviceId(McHue.BRIDGE_DATA.getProperty(BridgeProperties.DEVICE_INDENTIFIER));
                break DEVICE_ID_CHECK;
            }

            McHue.LOGGER.debug("No device identifier, generating it.");
            try {
                bridge.setDeviceId("mchue#" + InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                bridge.setDeviceId("mchue#unknown");
            }
        }

        if (bridge.getUsername() == null || bridge.getClientKey() == null) {
            if (bridgeIsInConfig) {
                bridge.setUsername(McHue.BRIDGE_DATA.getProperty(BridgeProperties.USERNAME));
                bridge.setClientKey(McHue.BRIDGE_DATA.getProperty(BridgeProperties.CLIENT_KEY));
                return true;
            }

            McHue.LOGGER.log(Level.INFO, "No username or client key, generating it.");

            String url = "http://" + bridge.getBridgeIp() + "/api";
            String data = "{\"devicetype\":\"" + bridge.getDeviceId() + "\", \"generateclientkey\":true}";

            // only valid for 30 attempts
            UsernameCreator uc = new UsernameCreator(url, data, bridge, updateMethod);

            t = scheduler.scheduleAtFixedRate(uc, 0, DEFAULT_POLL_INTERVAL, TimeUnit.SECONDS);
            return false;
        }
        return true;
    }

    public void cancelConnection() {
        t.cancel(true);
    }

    private class UsernameCreator implements Runnable {
        private final String url;
        private final String data;
        private final HueBridge bridge;
        private final Consumer<BridgeConnectionUpdate> updateMethod;
        private int attempt = 1;

        protected UsernameCreator(String url, String data, HueBridge bridge, @Nullable Consumer<BridgeConnectionUpdate> updateMethod) {
            this.url = url;
            this.data = data;
            this.bridge = bridge;
            this.updateMethod = updateMethod;
        }

        @Override
        public void run() {
            if (attempt >= MAX_ATTEMPTS) {
                if (updateMethod != null)
                    updateMethod.accept(new BridgeConnectionUpdate(BridgeResponse.TIME_UP, null, null));
                t.cancel(false);
                return;
            }

            //todo: add more useful solutions to exceptions eg. "Are you connected to internet?"
            Optional<JsonNode> response = NetworkUtil.postJson(url, data);
            if (response.isEmpty()) {
                if (updateMethod != null)
                    updateMethod.accept(new BridgeConnectionUpdate(BridgeResponse.FAILURE, null, "Failed sending the request to the bridge"));
                return;
            }

            JsonNode parsedResponse = response.get().query("[0]");
            if (parsedResponse.has("success")) {
                scheduler.shutdown();
                bridge.setUsername(parsedResponse.query("success.username").asString());
                bridge.setClientKey(parsedResponse.query("success.clientkey").asString());

                if (updateMethod != null)
                    updateMethod.accept(new BridgeConnectionUpdate(BridgeResponse.SUCCESS, null, null));

            } else if (parsedResponse.has("error") && parsedResponse.query("error.type").asInt() == 101) {
                if (updateMethod != null)
                    updateMethod.accept(new BridgeConnectionUpdate(BridgeResponse.PRESS_BUTTON, DEFAULT_POLL_INTERVAL * (MAX_ATTEMPTS - attempt), null));

            } else {
                LOGGER.error("The bridge responded in an unknown way: " + parsedResponse);
                if (updateMethod != null)
                    updateMethod.accept(new BridgeConnectionUpdate(BridgeResponse.FAILURE, null, "The bridge responded in an unknown way. Check logs for more info."));
            }
            attempt++;
        }
    }
}
