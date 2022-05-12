package codes.ztereohype.mchue.devices.interfaces;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public final class LightState {
    private double r;
    private double g;
    private double b;
    public @Getter @Setter boolean powered;

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
     * @param rgb the rgb integer from which to set the red, green, and blue values
     *            (rgb = 0xRRGGBB)
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
     */
    public LightState(int r, int g, int b) {
        this(r, g, b, true);
    }

    /**
     * @return the brightness value between 0 and 1
     */
    public double getBrightness() {
        return (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }

    /**
     * @return the hue value between 0 and 1
     */
    public double getHue() {
        return (0.5 * (r - g + r - b));
    }

    /**
     * @return the saturation value between 0 and 1
     */
    public double getSaturation() {
        return (Math.max(Math.max(r, g), b) - Math.min(Math.min(r, g), b));
    }

    /**
     * @return the red value between 0 and 255
     */
    public int getRedI() {
        return (int) (r * 255);
    }

    /**
     * @return the green value between 0 and 255
     */
    public int getGreenI() {
        return (int) (g * 255);
    }

    /**
     * @return the blue value between 0 and 255
     */
    public int getBlueI() {
        return (int) (b * 255);
    }

    /**
     * @param r the red value between 0 and 255 to set
     */
    public void setRedI(int r) {
        this.r = r / 255D;
    }

    /**
     * @param g the green value between 0 and 255 to set
     */
    public void setGreenI(int g) {
        this.g = g / 255D;
    }

    /**
     * @param b the blue value between 0 and 255 to set
     */
    public void setBlueI(int b) {
        this.b = b / 255D;
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

    /**
     * @param r the red value between 0 and 1 to set
     */
    public void setRedF(float r) {
        this.r = r;
    }

    /**
     * @param g the green value between 0 and 1 to set
     */
    public void setGreenF(float g) {
        this.g = g;
    }

    /**
     * @param b the blue value between 0 and 1 to set
     */
    public void setBlueF(float b) {
        this.b = b;
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
