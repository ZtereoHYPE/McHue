package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.devices.interfaces.LightState;
import codes.ztereohype.mchue.util.NetworkUtil;
import lombok.Getter;
import lombok.Setter;
import net.shadew.json.JsonNode;
import net.shadew.json.JsonPath;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


// this class will represent a single light that will accept various controls
public class HueLight {
    //todo change to a getter system
    private final @Getter String id;
    private final @Getter String index;
    private final @Getter String name;
    private final String POST_ENDPOINT;
    private final String GET_ENDPOINT;
    private @Getter @Setter boolean active = false;

    private LightState lastState;

    private static final JsonPath ON_PATH = JsonPath.parse("state.on");
    private static final JsonPath BRI_PATH = JsonPath.parse("state.bri");
    private static final JsonPath HUE_PATH = JsonPath.parse("state.hue");
    private static final JsonPath SAT_PATH = JsonPath.parse("state.sat");

    public HueLight(String id, String index, String name, HueBridge parentBridge) {
        this.id = id;
        this.index = index;
        this.name = name;
        this.POST_ENDPOINT = "http://" + parentBridge.getBridgeIp() + "/api/" + parentBridge.getUsername() + "/lights/" + this.index + "/state";
        this.GET_ENDPOINT = "http://" + parentBridge.getBridgeIp() + "/api/" + parentBridge.getUsername() + "/lights/" + this.index;
    }

    public boolean setColour(@NotNull LightState colour) {
        if (colour.equals(lastState)) return true;
        lastState = colour;

        int transitionTime = 5; //todo: unhardcode transition time (load from config)

        //todo: dont send the on attribute if already on.
        String body = "{\"on\":" + colour.isPowered() + ", \"sat\":" + (int) (colour.getSaturation() * 255) + ", \"bri\":" + (int) (colour.getBrightness() * 254) + ", \"hue\":" + (int) (colour.getHue() / 360D * 255 * 256) + ", \"transitiontime\":" + transitionTime + "}";
        return NetworkUtil.putJson(POST_ENDPOINT, body);
    }

    public LightState getState() {
        Optional<JsonNode> light = NetworkUtil.getJson(GET_ENDPOINT);

        // default to some random off state
        if (light.isEmpty()) return new LightState(255, 0, 0, false);

        JsonNode lightJson = light.get();

        boolean state = lightJson.query(ON_PATH).asBoolean();
        float hue = lightJson.query(HUE_PATH).asFloat() / 256.0F;
        float sat = lightJson.query(SAT_PATH).asFloat() / 255.0F;
        float brightness = lightJson.query(BRI_PATH).asInt() / 254.0F;

        //todo: fix this
        return new LightState(hue, sat, brightness, state);
    }
}
