package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.devices.interfaces.LightState;
import codes.ztereohype.mchue.util.NetworkUtil;
import lombok.Getter;
import net.shadew.json.JsonNode;
import net.shadew.json.JsonPath;

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

    private final JsonPath ON_PATH = JsonPath.parse("state.on");
    private final JsonPath BRI_PATH = JsonPath.parse("state.bri");
    private final JsonPath HUE_PATH = JsonPath.parse("state.hue");
    private final JsonPath SAT_PATH = JsonPath.parse("state.sat");

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

        String body = "{\"on\":" + colour.isPowered() + ", \"sat\":" + colour.getSaturation() + ", \"bri\":" + colour.getBrightness() + ",\"hue\":" + colour.getHue() + "\"transitiontime\": 1" + "}";
        return NetworkUtil.putJson(POST_ENDPOINT, body);
    }

    public LightState getState() {
        Optional<JsonNode> light = NetworkUtil.getJson(GET_ENDPOINT);

        // default to some random off state
        if (light.isEmpty()) return new LightState(255, 0, 0, false);

        JsonNode lightJson = light.get();
        boolean state = lightJson.query(ON_PATH).asBoolean();
        int brightness = lightJson.query(BRI_PATH).asInt();
        int hue = lightJson.query(HUE_PATH).asInt();
        int saturation = lightJson.query(SAT_PATH).asInt();

        return new LightState(brightness, hue, saturation, state);
    }
}
