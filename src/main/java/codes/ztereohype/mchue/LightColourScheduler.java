package codes.ztereohype.mchue;

import codes.ztereohype.mchue.devices.HueLight;
import codes.ztereohype.mchue.devices.interfaces.LightState;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LightColourScheduler {
    private static final Map<HueLight, LightState> previousColours = new HashMap<>();
    private static ScheduledExecutorService scheduler;
    private static ColourGrabber cg;

    private static void updateColour() {
        McHue.activeBridge.streamColour(cg.getColour());
    }

    public static void startUpdater() {
        if (McHue.activeBridge == null) return;

        for (HueLight activeLight : McHue.activeBridge.getActiveLights()) {
            previousColours.put(activeLight, activeLight.getState());
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
