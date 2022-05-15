package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.devices.interfaces.LightState;
import codes.ztereohype.mchue.util.ColourUtil;
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
    private final JsonPath X_PATH = JsonPath.parse("state.xy[0]");
    private final JsonPath Y_PATH = JsonPath.parse("state.xy[1]");

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

        //todo: unhardcode transition time?
        String body = "{\"on\":" + colour.isPowered() + ", \"sat\":" + (int) (colour.getSaturation() * 255) + ", \"bri\":" + (int) (colour.getBrightness() * 254) + ", \"hue\":" + (int) (colour.getHue()/360D * 255 * 256) + ", \"transitiontime\": 5" + "}";
        return NetworkUtil.putJson(POST_ENDPOINT, body);
    }

    public LightState getState() {
        Optional<JsonNode> light = NetworkUtil.getJson(GET_ENDPOINT);

        // default to some random off state
        if (light.isEmpty()) return new LightState(255, 0, 0, false);

        JsonNode lightJson = light.get();

        boolean state = lightJson.query(ON_PATH).asBoolean();
        int brightness = lightJson.query(BRI_PATH).asInt();
        float x = lightJson.query(X_PATH).asFloat();
        float y = lightJson.query(Y_PATH).asFloat();

        return ColourUtil.xyToLightState(x, y, brightness, state);
    }
}
