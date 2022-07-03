package codes.ztereohype.mchue;

import codes.ztereohype.mchue.devices.HueLight;
import codes.ztereohype.mchue.devices.interfaces.LightState;
import codes.ztereohype.mchue.mixin.ui.MixinDebug;
import org.apache.logging.log4j.Level;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LightColourScheduler {
    private static final Map<HueLight, LightState> previousColours = new HashMap<>();
    private static ScheduledExecutorService scheduler;
    public static ColourGrabber cg;

    private static void updateColour() {
        LightState colour;
        try {
            colour = cg.getColour();
        } catch (Exception e) {
            McHue.LOGGER.error("Caught exception in LightColourScheduler. StackTrace:\n" + Arrays.toString(e.getStackTrace()));
            stopUpdater();
            return;
        }
        McHue.activeBridge.streamColour(colour);
        McHue.ld.setLightColour(colour);
    }

    public static void startUpdater() {
        if (McHue.activeBridge == null) return;

        for (String lightId : McHue.activeBridge.getActiveLights()) {
            HueLight light = McHue.activeBridge.bridgeLights.get(lightId);
            previousColours.put(light, light.getState());
        }

        cg = new ColourGrabber();
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(LightColourScheduler::updateColour, 0, 500, TimeUnit.MILLISECONDS);
    }

    public static void stopUpdater() {
        if (scheduler != null) scheduler.shutdownNow();

        for (HueLight modifiedLight : previousColours.keySet()) {
            McHue.LOGGER.log(Level.INFO, previousColours.get(modifiedLight));
            modifiedLight.setColour(previousColours.get(modifiedLight));
        }
    }
}
