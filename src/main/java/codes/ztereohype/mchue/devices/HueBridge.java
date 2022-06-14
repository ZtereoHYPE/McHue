package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.config.BridgeProperties;
import codes.ztereohype.mchue.devices.interfaces.LightState;
import codes.ztereohype.mchue.util.NetworkUtil;
import lombok.Getter;
import net.shadew.json.JsonNode;
import net.shadew.json.JsonPath;

import java.util.*;
import java.util.stream.Collectors;

public class HueBridge {
    private final @Getter String bridgeId;
    private final @Getter String bridgeIp;
    public List<HueLight> connectedLights = new ArrayList<>();
    private @Getter String deviceId;
    private @Getter String username;
    private @Getter String clientKey;

    private final JsonPath NAME_PATH = JsonPath.parse("name");
    private final JsonPath ID_PATH = JsonPath.parse("uniqueid");

    public HueBridge(String id, String ip) {
        this.bridgeId = id;
        this.bridgeIp = ip;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public boolean isComplete() {
        return username != null && deviceId != null && bridgeIp != null && bridgeId != null;
    }

    public boolean scanLights() {
        if (!isComplete()) return false;
        String endpoint = "http://" + getBridgeIp() + "/api/" + username + "/lights";
        Optional<JsonNode> response = NetworkUtil.getJson(endpoint);

        if (response.isEmpty()) {
            return false;
        }

        connectedLights.clear();

        for (String lightKey : response.get().keySet()) {
            JsonNode lightJson = response.get().get(lightKey);
            String id = lightJson.query(ID_PATH).asString();
            String name = lightJson.query(NAME_PATH)
                                   .asString(); //node: maybe append model or room? or add it in the class?
            HueLight light = new HueLight(id, lightKey, name, this);

            if (Arrays.asList(McHue.BRIDGE_DATA.getPropertyArray(BridgeProperties.CONNECTED_LIGHTS)).contains(id)) {
                light.setActive(true);
            }

            connectedLights.add(light);
        }
        return true;
    }

    public void setActiveLight(String lightId, boolean active) {
        Optional<HueLight> light = connectedLights.stream().filter(l -> l.getId().equals(lightId)).findFirst();
        if (light.isEmpty()) return;

        light.get().setActive(active);
    }

    public Set<HueLight> getActiveLights() {
        return connectedLights.stream().filter(l -> l.isActive()).collect(Collectors.toSet());
    }

    //todo: make this have 2 branches: V1 and V2 (that uses the streaming api) (can both be used at the same time maybe??)
    public void streamColour(LightState colour) {
        for (HueLight light : connectedLights.stream().filter(HueLight::isActive).toArray(HueLight[]::new)) {
            light.setColour(colour);
        }
    }
}
