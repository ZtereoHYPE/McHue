package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.config.BridgeProperties;
import codes.ztereohype.mchue.util.NetworkUtil;
import lombok.Getter;
import net.shadew.json.JsonNode;

import java.util.*;
import java.util.stream.Collectors;

// this class will represent a single Hue Bridge
public class HueBridge {
    private final @Getter String bridgeId;
    private final @Getter String bridgeIp;
    private @Getter String username;
    private @Getter String token;
    public boolean passedConnectionTest = false;
    public List<HueLight> connectedLights = new ArrayList<>();

    public HueBridge(String id, String ip) {
        this.bridgeId = id;
        this.bridgeIp = ip;
    }

    public void setUsername(String username) {
        this.username = username;
        if (this.token != null) {
            passedConnectionTest = scanLights();
        }
    }

    public void setToken(String token) {
        this.token = token;
        if (this.username != null) {
            passedConnectionTest = scanLights();
        }
    }

    public boolean isComplete() {
        return token != null && username != null && bridgeIp != null && bridgeId != null;
    }

    public boolean scanLights() {
        if (!isComplete()) return false;
        String endpoint = "http://" + getBridgeIp() + "/api/" + token + "/lights";
        Optional<JsonNode> response = NetworkUtil.getJson(endpoint);

        if (response.isEmpty()) {
            return false;
        }

//        connectedLights.clear();
        for (String lightKey : response.get().keySet()) {
            JsonNode lightJson = response.get().get(lightKey);
            String id = lightJson.query("uniqueid").asString();
            String name = lightJson.query("name")
                                   .asString(); //node: maybe append model or room? or add it in the class?
            HueLight light = new HueLight(id, lightKey, name, this);

            //todo move out of here????
            if (Arrays.asList(McHue.BRIDGE_DATA.getPropertyArray(BridgeProperties.CONNECTED_LIGHTS)).contains(id))
                light.active = true;
            //todo: use room instead of ID in the UI; much more intuitive oh no its in the

            //todo: SINCE WHEN IS ANYMATCH A THING; INSTANTLY REPLACE IT IN ALL STEREAMSS
            if (connectedLights.stream().noneMatch(l -> l.getId().equals(id))) connectedLights.add(light);
        }
        return true;
    }

    public void setActiveLight(String lightId, boolean active) {
        Optional<HueLight> light = connectedLights.stream().filter(l -> l.getId().equals(lightId)).findFirst();
        if (light.isEmpty()) return;

        light.get().active = active;
    }

    public Set<HueLight> getActiveLights() {
        return connectedLights.stream().filter(l -> l.active).collect(Collectors.toSet());
    }

    public void streamColour(int rgb) {
        for (HueLight light : connectedLights.stream().filter(l -> l.active).toArray(HueLight[]::new)) {
            if (light.active) light.setColour(rgb);
        }
    }
}
