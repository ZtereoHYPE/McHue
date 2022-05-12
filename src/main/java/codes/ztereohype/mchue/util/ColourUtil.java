package codes.ztereohype.mchue.util;

import codes.ztereohype.mchue.devices.interfaces.LightState;

//todo: remove the my when all the code is migrated, and maybe change to xy system?
public class ColourUtil {
    //todo: add correction for vision maybe (dark should be less dark, we see in ~logarithmic)
//    public static LightState getLightState(int rgb) {
////        int r = (rgb >> 16) & 0xff;
////        int g = (rgb >> 8) & 0xff;
////        int b = (rgb) & 0xff;
//
//        // HSB IS WRONG COLOURSPACE!! HUE TAKES HSL NOT HSB/HSV
//        // this lib is broken for hue
//        int r = (rgb >> 16) & 0xff;
//        int g = (rgb >> 8) & 0xff;
//        int b = (rgb) & 0xff;
//
//        float[] hsb = Color.RGBtoHSB(r, g, b, null);
//
//        int hue = (int) (hsb[0] * 256 * 256);
//        int sat = (int) (hsb[1] * 256);
//        int bri = (int) (hsb[2] * 256);
//
//        return new LightState(bri, hue, sat, true);
//    }

    // lightEffectStrength and brightness are 0 -> 1
    public static LightState blendLightColour(LightState colour, LightState lightColour, float lightEffectStrength, float brigthness) {
        //        get the light 0 -> 1 for each channel    shrink strengh down   shift it up so that max is still 1 but min is lightStrenght
        float lightR = lightColour.getRedF() * lightEffectStrength + (1.0F - lightEffectStrength);
        float lightG = lightColour.getGreenF() * lightEffectStrength + (1.0F - lightEffectStrength);
        float lightB = lightColour.getBlueF() * lightEffectStrength + (1.0F - lightEffectStrength);

        int r = (int) (colour.getRedI() * lightR * brigthness);
        int g = (int) (colour.getGreenI() * lightG * brigthness);
        int b = (int) (colour.getBlueI() * lightB * brigthness);

        return new LightState(r, g, b, true);
    }

    // t is 0..1
    public static LightState lerpColours(LightState colour1, LightState colour2, float t) {
        //todo: there must be a way to optimise all of this. for instance we are calculating rgb1 twice
        int r = (int) (colour1.getRedI() + (colour2.getRedI() - colour1.getRedI()) * t);
        int g = (int) (colour1.getGreenI() + (colour2.getGreenI() - colour1.getGreenI()) * t);
        int b = (int) (colour1.getRedI() + (colour2.getBlueI() - colour1.getBlueI()) * t);

        return new LightState(r, g, b);
    }
}
