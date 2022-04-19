package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.config.BridgeProperties;
import codes.ztereohype.mchue.util.NetworkUtil;
import lombok.Getter;
import net.shadew.json.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// this class will represent a single Hue Bridge
public class HueBridge {
    private final @Getter String id;
    private final @Getter String ip;
    private @Getter String userId;
    private @Getter String username; //todo: refactor to use user and token instead of userid and username
    public boolean passedConnectionTest = false;
    public List<HueLight> connectedLights = new ArrayList<>();

    public HueBridge(String id, String ip) {
        this.id = id;
        this.ip = ip;
    }

    public HueBridge(String id, String ip, String userId, String username) {
        this.id = id;
        this.ip = ip;
        this.userId = userId;
        this.username = username;
        this.passedConnectionTest = scanLights();
    }

    public void setUserId(String userId) {
        this.userId = userId;
        if (this.username != null) {
            passedConnectionTest = scanLights();
        }
    }

    public void setUsername(String username) {
        this.username = username;
        if (this.userId != null) {
            passedConnectionTest = scanLights();
        }
    }

    public boolean isComplete() {
        return username != null && userId != null && ip != null && id != null;
    }

    public boolean scanLights() {
        if (!isComplete()) return false;
        String endpoint = "http://" + getIp() + "/api/" + username + "/lights";
        Optional<JsonNode> response = NetworkUtil.getJson(endpoint);

        if (response.isEmpty()) {
            //todo: ui error
            return false;
        }

//        connectedLights.clear();
        for (String lightKey : response.get().keySet()) {
            JsonNode lightJson = response.get().get(lightKey);
            String id = lightJson.query("uniqueid").asString();
            String name = lightJson.query("name").asString(); //node: maybe append model or room? or add it in the class?
            HueLight light = new HueLight(id, lightKey, name, this);

            //todo move out of here????
            if (Arrays.asList(McHue.BRIDGE_DATA.getPropertyArray(BridgeProperties.CONNECTED_LIGHTS)).contains(id)) light.active = true;
            //todo: use room instead of ID in the UI; much more intuitive

            //todo: SINCE WHEN IS ANYMATCH A THING; INSTANTLY REPLACE IT IN ALL STEREAMSS
            if (!connectedLights.stream().anyMatch(l -> l.ID.equals(id))) connectedLights.add(light);
        }
        return true;
    }

    public void setActiveLight(String lightId, boolean active) {
        Optional<HueLight> light = connectedLights.stream().filter(l -> l.ID.equals(lightId)).findFirst();
        if (light.isEmpty()) return;

        light.get().active = active;
    }

    public void streamColour(int rgb) {
        for (HueLight light : connectedLights.stream().filter(l -> l.active).toArray(HueLight[]::new)) {
            if (light.active) light.setColour(rgb);
        }
    }
}
