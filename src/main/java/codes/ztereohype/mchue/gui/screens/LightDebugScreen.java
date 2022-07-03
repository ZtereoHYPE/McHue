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
    private @Setter LightState lightColour = new LightState(255, 255, 255);
    private final Minecraft mc;

    public LightDebugScreen(Minecraft mc) {
        this.mc = mc;
    }

    //todo: update the info here and not at every frame (call every x frames?)
    public void tick() {

    }

    public void render(PoseStack poseStack) {
        this.renderLightMap(poseStack, LightColourScheduler.cg.getBLockLight(), LightColourScheduler.cg.getSkyLight());
        this.renderDebugLight(poseStack, lightColour);
    }

    private void renderLightMap(PoseStack poseStack, int currentBlockLight, int currentSkyLight) {
        Minecraft mc = Minecraft.getInstance();
        int pixelSize = 6;

        NativeImage lightPixels = ((LightTextureAccessor) mc.gameRenderer.lightTexture()).getLightPixels();
        for (int x = 0; x < lightPixels.getWidth(); x++) {
            for (int y = 0; y < lightPixels.getHeight(); y++) {
                int colour = lightPixels.getPixelRGBA(x, y);

                int xCoord = mc.getWindow().getGuiScaledWidth() - (x * pixelSize);
                int yCoord = mc.getWindow().getGuiScaledHeight() - (y * pixelSize);

                GuiComponent.fill(poseStack, xCoord, yCoord, xCoord + pixelSize, yCoord + pixelSize, colour);
            }
        }
        GuiComponent.drawCenteredString(poseStack, mc.font, "x", mc.getWindow().getGuiScaledWidth() - (currentBlockLight * pixelSize) - pixelSize/2, mc.getWindow().getGuiScaledHeight() + (currentSkyLight * pixelSize) - pixelSize/2 - mc.font.lineHeight/2, FastColor.ARGB32.color(255, 255, 0 ,0));
    }

    private void renderDebugLight(PoseStack poseStack, LightState colour) {
        int intcolor = FastColor.ARGB32.color(200, colour.getRedI(), colour.getGreenI(), colour.getBlueI());

        int guiScaledWidth = mc.getWindow().getGuiScaledWidth();
        int guiScaledHeight = mc.getWindow().getGuiScaledHeight();

        int xCoord = guiScaledWidth/4;
        int yCoord = guiScaledHeight - 20;

        GuiComponent.fill(poseStack, xCoord, yCoord, xCoord + guiScaledWidth/2, yCoord + 20, intcolor);
    }
}
