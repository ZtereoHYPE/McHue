package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.config.BridgeProperties;
import codes.ztereohype.mchue.devices.interfaces.LightState;
import codes.ztereohype.mchue.util.NetworkUtil;
import lombok.Getter;
import lombok.Setter;
import net.shadew.json.JsonNode;
import net.shadew.json.JsonPath;

import java.util.*;
import java.util.stream.Collectors;

// this class will represent a single Hue Bridge
public class HueBridge {
    private final @Getter String bridgeId;
    private final @Getter String bridgeIp;
    public boolean passedConnectionTest = false;
    public List<HueLight> connectedLights = new ArrayList<>();
    private @Getter String username;
    private @Getter String token;
    private @Getter String clientKey;

    private final JsonPath NAME_PATH = JsonPath.parse("name");
    private final JsonPath ID_PATH = JsonPath.parse("uniqueid");

    public HueBridge(String id, String ip) {
        this.bridgeId = id;
        this.bridgeIp = ip;
    }

    public void setUsername(String username) {
        this.username = username;
        if (this.token != null && this.clientKey != null) {
            passedConnectionTest = scanLights();
        }
    }

    public void setToken(String token) {
        this.token = token;
        if (this.username != null && this.clientKey != null) {
            passedConnectionTest = scanLights();
        }
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
        if (this.username != null && this.token != null) {
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
            String id = lightJson.query(ID_PATH).asString();
            String name = lightJson.query(NAME_PATH)
                                   .asString(); //node: maybe append model or room? or add it in the class?
            HueLight light = new HueLight(id, lightKey, name, this);

            //todo move out of here????
            if (Arrays.asList(McHue.BRIDGE_DATA.getPropertyArray(BridgeProperties.CONNECTED_LIGHTS)).contains(id))
                light.active = true;

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

    //todo: make this have 2 branches: V1 and V2 (that uses the streaming api) (can both be used at the same time maybe??)
    public void streamColour(LightState colour) {
        for (HueLight light : connectedLights.stream().filter(l -> l.active).toArray(HueLight[]::new)) {
            light.setColour(colour);
        }
    }
}
