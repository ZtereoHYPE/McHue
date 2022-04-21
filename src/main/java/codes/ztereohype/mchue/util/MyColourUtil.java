package codes.ztereohype.mchue.util;

import codes.ztereohype.mchue.devices.responses.LightState;
import net.shadew.util.misc.ColorUtil;

//todo: remove the my when all the code is migrated, and maybe change to xy system?
public class MyColourUtil {
    //todo: add correction for device maybe?
    public static LightState getColour(int rgb) {
        int hue = (int) (ColorUtil.hued(rgb) * 256 - 1000); //the 1000 is the correction done by eye
        int sat = (int) (ColorUtil.saturationd(rgb) * 254);
        int bri = (int) (ColorUtil.lightnessd(rgb) * 254);

        return new LightState(bri, hue, sat, true);
    }
}
