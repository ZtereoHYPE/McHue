package codes.ztereohype.mchue.util;

import codes.ztereohype.mchue.devices.interfaces.LightState;

import java.awt.color.ColorSpace;

//todo: remove the my when all the code is migrated, and maybe change to xy system?
public class ColourUtil {
    //todo: redo this: skew the hue towards the light hue, but change the brightness of the light only depending on the light strenght
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
        int r = (int) (colour1.getRedI() + (colour2.getRedI() - colour1.getRedI()) * t);
        int g = (int) (colour1.getGreenI() + (colour2.getGreenI() - colour1.getGreenI()) * t);
        int b = (int) (colour1.getBlueI() + (colour2.getBlueI() - colour1.getBlueI()) * t);

        return new LightState(r, g, b);
    }

    //todo: figure this shit out lmao
    public static LightState xyToLightState(float x, float y, float brightness, boolean on) {
        ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);

        float Y = brightness / 255.0F;
        float X = (Y / y) * x;
        float Z = (Y / y) * (1.0f - x - y);

        float[] rgb = sRGB.fromCIEXYZ(new float[]{X, Y, Z});
//
//        System.out.println(X + " " + Y + " " + Z);
//
//        float r =  X * 1.656492f - Y * 0.354851f - Z * 0.255038f;
//        float g = -X * 0.707196f + Y * 1.655397f + Z * 0.036152f;
//        float b =  X * 0.051713f - Y * 0.121364f + Z * 1.011530f;
//
//        System.out.println(r + " " + g + " " + b);
//
//        float r = rgb[0] <= 0.0031308f ? 12.92f * rgb[0] : (float) ((1.0f + 0.055f) * Math.pow(rgb[0], (1.0f / 2.4f)) - 0.055f);
//        float g = rgb[1] <= 0.0031308f ? 12.92f * rgb[1] : (float) ((1.0f + 0.055f) * Math.pow(rgb[1], (1.0f / 2.4f)) - 0.055f);
//        float b = rgb[2] <= 0.0031308f ? 12.92f * rgb[2] : (float) ((1.0f + 0.055f) * Math.pow(rgb[2], (1.0f / 2.4f)) - 0.055f);

        float r = rgb[0];
        float g = rgb[1];
        float b = rgb[2];

        return new LightState((int) (r * 255), (int) (g * 255), (int) (b * 255), on);
    }
}
