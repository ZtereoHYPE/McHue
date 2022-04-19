package codes.ztereohype.mchue.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class DrawingUtil {
    public static void drawStrings(Minecraft minecraft, PoseStack poseStack, String string, int x, int y, int wrapWidth, int lines, int color) {
        while (string != null && string.endsWith("\n")) {
            string = string.substring(0, string.length() - 1);
        }
        List<FormattedText> strings = minecraft.font.getSplitter()
                                                    .splitLines(new TextComponent(string), wrapWidth, Style.EMPTY);
        for (int i = 0; i < strings.size(); i++) {
            FormattedText renderable = strings.get(i);
            FormattedCharSequence line = Language.getInstance().getVisualOrder(renderable);
            //todo: handle RTL languages
            minecraft.font.draw(poseStack, line, x, y + i * minecraft.font.lineHeight, color);
        }
    }
}
