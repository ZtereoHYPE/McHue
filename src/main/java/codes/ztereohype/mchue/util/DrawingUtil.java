package codes.ztereohype.mchue.util;

import codes.ztereohype.mchue.mixin.LightTextureAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class DrawingUtil {
    public static void drawStrings(Minecraft minecraft, PoseStack poseStack, String string, int x, int y, int wrapWidth, int lines, int color) {
        while (string != null && string.endsWith("\n")) {
            string = string.substring(0, string.length() - 1);
        }
        List<FormattedText> strings = minecraft.font.getSplitter()
                                                    .splitLines(Component.literal(string), wrapWidth, Style.EMPTY);
        for (int i = 0; i < strings.size(); i++) {
            FormattedText renderable = strings.get(i);
            FormattedCharSequence line = Language.getInstance().getVisualOrder(renderable);
            //todo: handle RTL languages
            minecraft.font.draw(poseStack, line, x, y + i * minecraft.font.lineHeight, color);
        }
    }

    public static void renderLightmapDebug(PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();

        NativeImage lightPixels = ((LightTextureAccessor) mc.gameRenderer.lightTexture()).getLightPixels();
        for (int x = 0; x < lightPixels.getWidth(); x++) {
            for (int y = 0; y < lightPixels.getHeight(); y++) {
                int colour = lightPixels.getPixelRGBA(x, y);

                int pixelSize = 6;

                int xCoord = mc.getWindow().getGuiScaledWidth() - (x * pixelSize);
                int yCoord = mc.getWindow().getGuiScaledHeight() - (y * pixelSize);

                GuiComponent.fill(poseStack, xCoord, yCoord, xCoord + pixelSize, yCoord + pixelSize, colour);
            }
        }
    }
}
