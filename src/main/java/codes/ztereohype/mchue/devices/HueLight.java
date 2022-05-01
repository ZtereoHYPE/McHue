package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.devices.responses.LightState;
import codes.ztereohype.mchue.util.NetworkUtil;
import lombok.Getter;
import net.shadew.json.JsonNode;

import java.util.Optional;


// this class will represent a single light that will accept various controls
public class HueLight {
    //todo change to a getter system
    private final @Getter String id;
    private final @Getter String index;
    private final @Getter String name;
    private final String POST_ENDPOINT;
    private final String GET_ENDPOINT;
    public boolean active = false;

    private LightState lastState;

    public HueLight(String id, String index, String name, HueBridge parentBridge) {
        this.id = id;
        this.index = index;
        this.name = name;
        this.POST_ENDPOINT = "http://" + parentBridge.getBridgeIp() + "/api/" + parentBridge.getToken() + "/lights/" + this.index + "/state";
        this.GET_ENDPOINT = "http://" + parentBridge.getBridgeIp() + "/api/" + parentBridge.getToken() + "/lights/" + this.index;
    }

    public boolean setColour(LightState colour) {
        if (colour.equals(lastState)) return true;
        lastState = colour;

        String body = "{\"on\":" + colour.state() + ", \"sat\":" + colour.saturation() + ", \"bri\":" + colour.brightness() + ",\"hue\":" + colour.hue() + "}";
        return NetworkUtil.putJson(POST_ENDPOINT, body);
    }

    public LightState getState() {
        Optional<JsonNode> light = NetworkUtil.getJson(GET_ENDPOINT);

        // default to some random off state
        if (light.isEmpty()) return new LightState(255, 0, 0, false);

        JsonNode lightJson = light.get();
        boolean state = lightJson.query("state.on").asBoolean();
        int brightness = lightJson.query("state.bri").asInt();
        int hue = lightJson.query("state.hue").asInt();
        int saturation = lightJson.query("state.sat").asInt();

        return new LightState(brightness, hue, saturation, state);
    }
}
