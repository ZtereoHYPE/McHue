package codes.ztereohype.mchue;


import codes.ztereohype.mchue.devices.interfaces.LightState;
import org.junit.jupiter.api.Test;

public class LightStateTest {
    @Test
    public void testHsvToRgb() {
        LightState colour1 = new LightState(359F,1F, 1F, true);
        LightState colour2 = new LightState(134F, 0.63F, 0.73F, false);

        assert(colour1.getRedI() == 255);
        assert(colour1.getGreenI() == 0);
        assert(colour1.getBlueI() == 4);

        assert(colour2.getRedI() == 69);
        assert(colour2.getGreenI() == 186);
        assert(colour2.getBlueI() == 96);
    }

    @Test
    public void testHsvToHsv() {
        LightState colour1 = new LightState(359F,1F, 1F, true);
        LightState colour2 = new LightState(134F, 0.63F, 0.73F, false);

        assert(Math.round(colour1.getHue()*100)/100f == 359F);
        assert(Math.round(colour1.getSaturation()*100)/100f == 1.0F);
        assert(Math.round(colour1.getBrightness()*100)/100f == 1.0F);

        assert(Math.round(colour2.getHue()*100)/100f == 134F);
        assert(Math.round(colour2.getSaturation()*100)/100f == 0.63F);
        assert(Math.round(colour2.getBrightness()*100)/100f == 0.73F);
    }
}
