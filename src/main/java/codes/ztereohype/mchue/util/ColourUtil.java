package codes.ztereohype.mchue.util;

import codes.ztereohype.mchue.devices.interfaces.LightState;

//todo: remove the my when all the code is migrated, and maybe change to xy system?
public class ColourUtil {
    //todo: add correction for vision maybe (dark should be less dark, we see in ~logarithmic)
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

    public static LightState xyToLightState(float x, float y, int Y, boolean on) {
        float z = 1.0f - x - y;
        float X = (Y / y) * x;
        float Z = (Y / y) * z;

        double r =  X * 1.656492f - Y * 0.354851f - Z * 0.255038f;
        double g = -X * 0.707196f + Y * 1.655397f + Z * 0.036152f;
        double b =  X * 0.051713f - Y * 0.121364f + Z * 1.011530f;

        r = r <= 0.0031308f ? 12.92f * r : (1.0f + 0.055f) * Math.pow(r, (1.0f / 2.4f)) - 0.055f;
        g = g <= 0.0031308f ? 12.92f * g : (1.0f + 0.055f) * Math.pow(g, (1.0f / 2.4f)) - 0.055f;
        b = b <= 0.0031308f ? 12.92f * b : (1.0f + 0.055f) * Math.pow(b, (1.0f / 2.4f)) - 0.055f;

        return new LightState((int) (r * 255), (int) (g * 255), (int) (b * 255), on);
    }
}
