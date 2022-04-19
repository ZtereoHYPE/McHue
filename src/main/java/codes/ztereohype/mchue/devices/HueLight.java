package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.util.NetworkUtil;
import lombok.Getter;
import net.shadew.util.misc.ColorUtil;

import java.io.IOException;


// this class will represent a single light that will accept various controls
public class HueLight {
    //todo change to a getter system
    public final String ID;
    public final String INDEX;
    public final String NAME;
    public final HueBridge PARENT_BRIDGE;
    private final String ENDPOINT;
    public boolean active = false;

    public HueLight(String id, String index, String name, HueBridge parentBridge) {
        ID = id;
        INDEX = index;
        NAME = name;
        PARENT_BRIDGE = parentBridge;
        ENDPOINT = "http://" + PARENT_BRIDGE.getIp() + "/api/" + PARENT_BRIDGE.getUsername() + "/lights/" + INDEX + "/state";
    }

    public boolean setColour(int rgb) {
        int sat = (int) (ColorUtil.saturationd(rgb) * 254);
        int bri = (int) (ColorUtil.lightnessd(rgb) * 254);
        int hue = (int) (ColorUtil.hued(rgb) * 256 - 1000);

        System.out.println(sat + " " + bri + " " + hue);

        String body = "{\"on\":true, \"sat\":" + sat + ", \"bri\":" + bri + ",\"hue\":" + hue + "}";

        return NetworkUtil.putJson(ENDPOINT, body);
    }

    public boolean power(boolean power) throws IOException, InterruptedException {
        String body = "{\"on\":" + power + "}";
        return NetworkUtil.putJson(ENDPOINT, body);
    }
}
