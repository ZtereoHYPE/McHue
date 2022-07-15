package codes.ztereohype.mchue.devices.interfaces;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public final class LightState {
    private double r;
    private double g;
    private double b;
    public @Getter @Setter boolean powered;

    private boolean isGammaCorrected = false;

    /**
     * @param r       the red value between 0 and 255 to set
     * @param g       the green value between 0 and 255 to set
     * @param b       the blue value between 0 and 255 to set
     * @param powered the power state to set (true = on, false = off)
     * @throws IllegalArgumentException if the red, green or blue value is not between 0 and 255
     */
    public LightState(int r, int g, int b, boolean powered) {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IllegalArgumentException("The red, green, or blue values are not between 0 and 255");
        }
        this.powered = powered;
        this.r = r / 255D;
        this.g = g / 255D;
        this.b = b / 255D;
    }

    /**
     * @param rgb the rgb integer from which to set the red, green, and blue values: 0xRRGGBB.
     *            <p>
     *            The red, green, and blue values are between 0 and 255.
     *            The alpha value is ignored.
     *            <p>
     *            The power state is set to true.
     */
    public LightState(int rgb) {
        this((rgb >> 16 & 0xFF), (rgb >> 8 & 0xFF), (rgb & 0xFF), true);
    }

    /**
     * @param r the red value between 0 and 255.
     * @param g the green value between 0 and 255.
     * @param b the blue value between 0 and 255.
     *
     *          <p>The power state is set to true</p>
     */
    public LightState(int r, int g, int b) {
        this(r, g, b, true);
    }

    /**
     * @param hue the hue value between 0 and 359.
     * @param sat the saturation value between 0 and 1.
     * @param val the value between 0 and 1.
     */
    public LightState(double hue, double sat, double val, boolean powered) {
        if (hue < 0 || hue > 359 || sat < 0 || sat > 1 || val < 0 || val > 1) {
            throw new IllegalArgumentException("The hue, saturation, or value are not between in their correct ranges");
        }

        this.isGammaCorrected = true;

        double c = val * sat;
        double x = c * (1 - Math.abs(hue / 60 % 2 - 1));
        int hueInterval = (int) (hue / 60);

        double ra, ga, ba;
        switch (hueInterval) {
            case 0 -> {
                ra = c;
                ga = x;
                ba = 0;
            }
            case 1 -> {
                ra = x;
                ga = c;
                ba = 0;
            }
            case 2 -> {
                ra = 0;
                ga = c;
                ba = x;
            }
            case 3 -> {
                ra = 0;
                ga = x;
                ba = c;
            }
            case 4 -> {
                ra = x;
                ga = 0;
                ba = c;
            }
            case 5 -> {
                ra = c;
                ga = 0;
                ba = x;
            }
            default -> { // else compiler cries
                ra = 0;
                ga = 0;
                ba = 0;
            }
        }

        double m = val - c;

        this.r = ra + m;
        this.g = ga + m;
        this.b = ba + m;
        this.powered = powered;
    }

    /**
     * Applies the gamma correction to the r g and b values.
     * From:
     * <a href="https://developers.meethue.com/develop/application-design-guidance/color-conversion-formulas-rgb-to-xy-and-back/">Philips Hue API Developer Guide</a>
     */
    public void applyGammaCorrection() {
        if (this.isGammaCorrected) return;
        this.r = (r > 0.04045f) ? Math.pow((r + 0.055f) / (1.0f + 0.055f), 2.4f) : (r / 12.92f);
        this.g = (g > 0.04045f) ? Math.pow((g + 0.055f) / (1.0f + 0.055f), 2.4f) : (g / 12.92f);
        this.b = (b > 0.04045f) ? Math.pow((b + 0.055f) / (1.0f + 0.055f), 2.4f) : (b / 12.92f);
    }

    /**
     * @return the brightness value between 0 and 1 with gamma correction
     */
    public double getBrightness() {
        return Math.max(Math.max(this.r, this.g), this.b);
    }

    /**
     * @return the hue value between 0 and 360 with gamma correction
     */
    public double getHue() {
        double min = Math.min(Math.min(this.r, this.g), this.b);
        double max = Math.max(Math.max(this.r, this.g), this.b);

        double delta = max - min;

        if (delta == 0) return 0;

        double hue;
        if (max == this.r) {
            hue = (this.g - this.b) / delta;

        } else if (max == this.g) {
            hue = 2D + (this.b - this.r) / delta;

        } else {
            hue = 4D + (this.r - this.g) / delta;
        }

        hue *= 60;
        hue = hue < 0 ? hue + 360 : hue;

        return Math.round(hue); // round to nearest whole degree?
    }

    /**
     * @return the saturation value between 0 and 1 with gamma correction
     */
    public double getSaturation() {
        double cmax = Math.max(Math.max(r, g), b);
        double cmin = Math.min(Math.min(r, g), b);

        return (cmax - cmin) / cmax;
    }

    /**
     * @return the red value between 0 and 255
     */
    public int getRedI() {
        return (int) Math.round(r * 255);
    }

    /**
     * @return the green value between 0 and 255
     */
    public int getGreenI() {
        return (int) Math.round(g * 255);
    }

    /**
     * @return the blue value between 0 and 255
     */
    public int getBlueI() {
        return (int) Math.round(b * 255);
    }

    /**
     * @return the red value between 0 and 1
     */
    public float getRedF() {
        return (float) r;
    }

    /**
     * @return the green value between 0 and 1
     */
    public float getGreenF() {
        return (float) g;
    }

    /**
     * @return the blue value between 0 and 1
     */
    public float getBlueF() {
        return (float) b;
    }

    @Override
    public String toString() {
        return "LightState [red=" + r + ", green=" + g + ", blue=" + b + ", powered=" + powered + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final LightState that = (LightState) obj;

        return this.r == that.r &&
                this.g == that.g &&
                this.b == that.b &&
                this.powered == that.powered;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, g, b, powered);
    }
}
