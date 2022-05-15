package codes.ztereohype.mchue.gui.widget.entries;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.devices.HueBridge;
import codes.ztereohype.mchue.gui.widget.BridgeSelectionList;
import codes.ztereohype.mchue.util.DrawingUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BridgeEntry extends ObjectSelectionList.Entry<BridgeEntry> {
    private final Minecraft minecraft = Minecraft.getInstance();
    private final @Getter HueBridge bridge;
    private final BridgeSelectionList parent;
    private final ResourceLocation BRIDGE_ICON = new ResourceLocation(McHue.MOD_ID, "textures/gui/bridge_icon.png");
    private boolean connected = false;

    public BridgeEntry(HueBridge bridge, BridgeSelectionList parent) {
        this.bridge = bridge;
        this.parent = parent;
        if (this.bridge.passedConnectionTest) connected = true;
    }

    @Override
    public Component getNarration() {
        //todo: proper accessibility
        return new TextComponent("Bridge");
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        int ICON_SIZE = 32;

        // icon
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BRIDGE_ICON);
        RenderSystem.enableBlend();
        GuiComponent.blit(poseStack, x, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        RenderSystem.disableBlend();

        // title
        Font font = this.minecraft.font;
        Component name = new TextComponent("Bridge " + (index + 1));
        this.minecraft.font.draw(poseStack, Language.getInstance()
                                                    .getVisualOrder(name), x + 3 + ICON_SIZE, y + 1, 0xFFFFFF);

        // label
        if (connected) {
            String labelContent = "Connected";
            int labelWidth = font.width(labelContent) + 6;
            int labelX = x + 5 + ICON_SIZE + font.width(name);
            int outlineColor = 0xff009933;
            int backgroundColor = 0xff115a24;
            int textColor = 0xffffff;

            //outlines
            GuiComponent.fill(poseStack, labelX + 1, y - 1, labelX + labelWidth, y, outlineColor);
            GuiComponent.fill(poseStack, labelX, y, labelX + 1, y + Minecraft.getInstance().font.lineHeight, outlineColor);
            GuiComponent.fill(poseStack, labelX + 1, y + 1 + Minecraft.getInstance().font.lineHeight - 1, labelX + labelWidth, y + Minecraft.getInstance().font.lineHeight + 1, outlineColor);
            GuiComponent.fill(poseStack, labelX + labelWidth, y, labelX + labelWidth + 1, y + Minecraft.getInstance().font.lineHeight, outlineColor);
            //background
            GuiComponent.fill(poseStack, labelX + 1, y, labelX + labelWidth, y + Minecraft.getInstance().font.lineHeight, backgroundColor);
            //text
            Minecraft.getInstance().font.draw(poseStack, labelContent, (labelX + 1 + (labelWidth - Minecraft.getInstance().font.width(labelContent)) / 2.0F), y + 1, textColor);
        }

        // description
        String summary = "IP: " + bridge.getBridgeIp() + System.lineSeparator() + "ID: " + bridge.getBridgeId();
        DrawingUtil.drawStrings(this.minecraft, poseStack, summary, x + 7 + ICON_SIZE, y + minecraft.font.lineHeight + 2, rowWidth - 7, 2, 0x808080);
    }

    @Override
    public boolean mouseClicked(double v, double v1, int i) {
        parent.setSelected(this);
        return true;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
