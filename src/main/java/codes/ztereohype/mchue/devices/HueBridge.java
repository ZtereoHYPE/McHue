package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.devices.interfaces.LightState;
import codes.ztereohype.mchue.network.NetworkUtil;
import lombok.Getter;
import lombok.Setter;
import net.shadew.json.JsonNode;
import net.shadew.json.JsonPath;

import java.util.*;

public class HueBridge {
    private final @Getter String bridgeId;
    private final @Getter String bridgeIp;
    public final HashMap<String, HueLight> bridgeLights = new HashMap<>();
    public final Set<String> activeLightIDs = new HashSet<>();
    private @Setter @Getter String deviceId;
    private @Setter @Getter String username;
    private @Setter @Getter String clientKey;

    private static final JsonPath LIGHT_NAME_PATH = JsonPath.parse("name");
    private static final JsonPath LIGHT_ID_PATH = JsonPath.parse("uniqueid");

    public HueBridge(String id, String ip) {
        this.bridgeId = id;
        this.bridgeIp = ip;
    }

    public HueBridge(String id, String ip, String deviceId, String username, String clientKey) {
        this.bridgeId = id;
        this.bridgeIp = ip;
        this.deviceId = deviceId;
        this.username = username;
        this.clientKey = clientKey;
        locateLights();
    }

    public boolean isComplete() {
        return username != null && deviceId != null && bridgeIp != null && bridgeId != null && clientKey != null;
    }

    public boolean locateLights() {
        if (!isComplete()) return false;
        String endpoint = "http://" + getBridgeIp() + "/api/" + getUsername() + "/lights";
        Optional<JsonNode> response = NetworkUtil.getJson(endpoint);

        if (response.isEmpty()) {
            return false;
        }

        bridgeLights.clear();

        for (String lightIndex : response.get().keySet()) {
            JsonNode lightJson = response.get().get(lightIndex);

            String id = lightJson.query(LIGHT_ID_PATH).asString();
            String name = lightJson.query(LIGHT_NAME_PATH).asString(); //node: maybe append model or room? or add it in the class?

            HueLight light = new HueLight(id, Integer.parseInt(lightIndex), name, this);
            bridgeLights.put(id, light);
        }
        return true;
    }

    public void setActiveLight(String lightId, boolean active) {
        if (!bridgeLights.containsKey(lightId)) {
            McHue.LOGGER.error("Light with id " + lightId + " not found in bridge.");
            return;
        }

        if (active) {
            activeLightIDs.add(lightId);
        } else {
            activeLightIDs.remove(lightId);
        }
    }

    public Set<String> getActiveLights() {
        return activeLightIDs;
    }

    //todo: make this have 2 branches: V1 and V2 (that uses the streaming api) (can both be used at the same time maybe??)
    public void streamColour(LightState colour) {
        colour.applyGammaCorrection();
        for (String lightID : activeLightIDs) {
            HueLight light = bridgeLights.get(lightID);
            light.setColour(colour);
        }
    }
}
