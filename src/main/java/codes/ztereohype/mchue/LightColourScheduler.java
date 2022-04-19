package codes.ztereohype.mchue;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Level;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LightColourScheduler {
    private static ScheduledExecutorService scheduler;
    private static ColourGrabber cg;

    private static void updateColour() {
        // TODO: make this safer
        int biomeName = cg.getBiome();
        //todo: hahahhaha this is shit lol
        McHue.ACTIVE_BRIDGE.streamColour(biomeName);
        //        McHue.LOGGER.log(Level.FATAL, "henlo, you are in biome " + Integer.toHexString(biomeName));
    }

    public static void startUpdater(Minecraft minecraft) {
        if (McHue.ACTIVE_BRIDGE == null) return;
        McHue.LOGGER.log(Level.INFO, "joined world");
        cg = new ColourGrabber(minecraft);
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(LightColourScheduler::updateColour, 0, 500, TimeUnit.MILLISECONDS);
    }

    public static void stopUpdater() {
        McHue.LOGGER.log(Level.INFO, "left world");
        if (scheduler != null) scheduler.shutdown();
    }
}
