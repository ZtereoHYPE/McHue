package codes.ztereohype.mchue.util;

import codes.ztereohype.mchue.devices.responses.LightState;

import java.awt.*;

//todo: remove the my when all the code is migrated, and maybe change to xy system?
public class MyColourUtil {
    //todo: add correction for device maybe?
    public static LightState getLightState(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;

        float[] hsb = Color.RGBtoHSB(r, g, b, null);

        int hue = (int) (hsb[0] * 256 * 255);
        int sat = (int) (hsb[1] * 256);
        int bri = (int) (hsb[2] * 256);

        return new LightState(bri, hue, sat, true);
    }

    // lightEffectStrength and brightness are 0 -> 1
    public static int blendLightColour(int rgb, int lightColour, float lightEffectStrength, float brigthness) {
        //              get the light 0 -> 1 for each channel    shrink strengh down   shift it up so that max is still 1 but min is lightStrenght
        float lightR = (((lightColour >> 16) & 0xff) / 255.0F) * lightEffectStrength + (1.0F - lightEffectStrength);
        float lightG = (((lightColour >> 8) & 0xff) / 255.0F) * lightEffectStrength + (1.0F - lightEffectStrength);
        float lightB = (((lightColour) & 0xff) / 255.0F) * lightEffectStrength + (1.0F - lightEffectStrength);

        System.out.println(lightR + " " + lightG + " " + lightB);

        int r = (int) (((rgb >> 16) & 0xff) * lightR * brigthness);
        int g = (int) (((rgb >> 8) & 0xff) * lightG * brigthness);
        int b = (int) (((rgb) & 0xff) * lightB * brigthness);

        return (r << 16) | (g << 8) | b;
    }
}
