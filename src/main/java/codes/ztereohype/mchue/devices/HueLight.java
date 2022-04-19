package codes.ztereohype.mchue.devices;

import codes.ztereohype.mchue.util.NetworkUtil;
import lombok.Getter;
import net.shadew.util.misc.ColorUtil;


// this class will represent a single light that will accept various controls
public class HueLight {
    //todo change to a getter system
    private final @Getter String id;
    private final @Getter String index;
    private final @Getter String name;
    private final String ENDPOINT;
    public boolean active = false;

    public HueLight(String id, String index, String name, HueBridge parentBridge) {
        this.id = id;
        this.index = index;
        this.name = name;
        this.ENDPOINT = "http://" + parentBridge.getBridgeIp() + "/api/" + parentBridge.getToken() + "/lights/" + this.index + "/state";
    }

    public boolean setColour(int rgb) {
        int sat = (int) (ColorUtil.saturationd(rgb) * 254);
        int bri = (int) (ColorUtil.lightnessd(rgb) * 254);
        int hue = (int) (ColorUtil.hued(rgb) * 256 - 1000);

        System.out.println(sat + " " + bri + " " + hue);

        String body = "{\"on\":true, \"sat\":" + sat + ", \"bri\":" + bri + ",\"hue\":" + hue + "}";

        return NetworkUtil.putJson(ENDPOINT, body);
    }

    public boolean power(boolean power) {
        String body = "{\"on\":" + power + "}";
        return NetworkUtil.putJson(ENDPOINT, body);
    }
}
