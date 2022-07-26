package codes.ztereohype.mchue.gui.screens;

import codes.ztereohype.mchue.LightColourScheduler;
import codes.ztereohype.mchue.devices.interfaces.LightState;
import codes.ztereohype.mchue.mixin.LightTextureAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.FastColor;

@Environment(EnvType.CLIENT)
public class LightDebugScreen extends GuiComponent {
    private final Minecraft mc;
    private @Setter LightState lightColour = new LightState(255, 255, 255);

    public LightDebugScreen(Minecraft mc) {
        this.mc = mc;
    }

    //todo: update the info here and not at every frame (call every x frames?)
    public void tick() {

    }

    public void render(PoseStack poseStack) {
        if (LightColourScheduler.cg == null) return;
        this.renderLightMap(poseStack, LightColourScheduler.cg.getBLockLight(), LightColourScheduler.cg.getSkyLight());
        this.renderDebugLight(poseStack, lightColour);
    }

    private void renderLightMap(PoseStack poseStack, int currentBlockLight, int currentSkyLight) {
        Minecraft mc = Minecraft.getInstance();
        int pixelSize = 6;

        NativeImage lightPixels = ((LightTextureAccessor) mc.gameRenderer.lightTexture()).getLightPixels();
        for (int x = 0; x < lightPixels.getWidth(); x++) {
            for (int y = 0; y < lightPixels.getHeight(); y++) {
                int colour = lightPixels.getPixelRGBA(x, y); // the format is reversed (ABGR)

                int b = colour & 0xFF;
                int g = colour >> 8 & 0xFF;
                int r = colour >> 16 & 0xFF;

                int xCoord = mc.getWindow().getGuiScaledWidth() - (x * pixelSize);
                int yCoord = mc.getWindow().getGuiScaledHeight() - (y * pixelSize);

                GuiComponent.fill(poseStack, xCoord, yCoord, xCoord + pixelSize, yCoord + pixelSize, 0xFF000000 | b << 16 | g << 8 | r);
            }
        }

        GuiComponent.drawCenteredString(poseStack,
                                        mc.font,
                                        "x",
                                        mc.getWindow().getGuiScaledWidth() - (currentBlockLight * pixelSize) - pixelSize / 2,
                                        mc.getWindow().getGuiScaledHeight() - (currentSkyLight * pixelSize) + pixelSize / 2 - mc.font.lineHeight / 2,
                                        FastColor.ARGB32.color(255, 255, 0, 0));

        GuiComponent.drawCenteredString(poseStack, mc.font,"Sky: " + currentSkyLight, mc.getWindow().getGuiScaledWidth() - (16 * pixelSize) - 20, mc.getWindow().getGuiScaledHeight() - (2 * mc.font.lineHeight), FastColor.ARGB32.color(255, 255, 128, 255));
        GuiComponent.drawCenteredString(poseStack, mc.font,"Block: " + currentBlockLight, mc.getWindow().getGuiScaledWidth() - (16 * pixelSize) - 20, mc.getWindow().getGuiScaledHeight() - (4 * mc.font.lineHeight), FastColor.ARGB32.color(255, 255, 128, 255));
    }

    private void renderDebugLight(PoseStack poseStack, LightState colour) {
        LightState correctedColor = new LightState(colour.getHue(), colour.getSaturation(), 1F, true);
        correctedColor.applyGammaCorrection();

        int intcolor = FastColor.ARGB32.color((int) (colour.getBrightness() * 255), correctedColor.getRedI(), correctedColor.getGreenI(), correctedColor.getBlueI());

        int guiScaledWidth = mc.getWindow().getGuiScaledWidth();
        int guiScaledHeight = mc.getWindow().getGuiScaledHeight();

        int xCoord = guiScaledWidth / 4;
        int yCoord = guiScaledHeight - 20;

        GuiComponent.fill(poseStack, xCoord, yCoord, xCoord + guiScaledWidth / 2, yCoord + 20, intcolor);
    }
}
