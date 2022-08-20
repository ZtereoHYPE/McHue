package codes.ztereohype.mchue.gui.widget;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.devices.HueBridge;
import codes.ztereohype.mchue.gui.screens.LightSelectionScreen;
import codes.ztereohype.mchue.util.DrawingUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BridgeSelectionList extends AbstractSelectionList<BridgeSelectionList.BridgeEntry> {
    private final LightSelectionScreen parent;

    private Entry<BridgeEntry> hovered;

    public BridgeSelectionList(Minecraft minecraft, int x0, int x1, int y0, int y1, int itemHeight, LightSelectionScreen parent) {
        super(minecraft, x1 - x0, y1 - y0, y0, y1, itemHeight);
        this.x0 = x0;
        this.x1 = x1;
        this.parent = parent;
    }

    @Override
    public void setSelected(BridgeEntry entry) {
        super.setSelected(entry);
    }

    //todo: accessibility (this is a test?)
    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        String narration = "Bridge Selection List";
        if (this.isFocused()) {
            narrationElementOutput.add(NarratedElementType.TITLE, narration);
        }
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;

        //background
        RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(this.x0, this.y1, 0.0)
                     .uv(this.x0 / f, (this.y1 + (int) this.getScrollAmount()) / f)
                     .color(32, 32, 32, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x1, this.y1, 0.0)
                     .uv(this.x1 / f, (this.y1 + (int) this.getScrollAmount()) / f)
                     .color(32, 32, 32, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x1, this.y0, 0.0)
                     .uv(this.x1 / f, (this.y0 + (int) this.getScrollAmount()) / f)
                     .color(32, 32, 32, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x0, this.y0, 0.0)
                     .uv(this.x0 / f, (this.y0 + (int) this.getScrollAmount()) / f)
                     .color(32, 32, 32, 255)
                     .endVertex();
        tesselator.end();


        int k = this.getRowLeft();
        int l = this.y0 + 4 - (int) this.getScrollAmount();

        // list
        this.renderList(poseStack, k, l, mouseX, mouseY, partialTick);

        // top and bottom
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(519);
        float g = 32.0F;
        int z = -100;
        int bottomDistance = this.parent.height - this.y1;
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        //top?
        bufferBuilder.vertex(this.x0, this.y0, z).uv(0.0F, this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.x0 + this.width, this.y0, z).uv(this.width / f, this.y0 / f)
                     .color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.x0 + this.width, 0.0, z).uv(this.width / f, 0.0F).color(64, 64, 64, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x0, 0.0, z).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();

        //bottom?
        bufferBuilder.vertex(this.x0, this.y1 + bottomDistance, z).uv(0.0F, (this.y1 + bottomDistance) / f)
                     .color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.x0 + this.width, this.y1 + bottomDistance, z)
                     .uv(this.width / f, (this.y1 + bottomDistance) / f).color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.x0 + this.width, this.y1, z).uv(this.width / f, this.y1 / f)
                     .color(64, 64, 64, 255).endVertex();
        bufferBuilder.vertex(this.x0, this.y1, z).uv(0.0F, this.y1 / f).color(64, 64, 64, 255).endVertex();
        tesselator.end();

        // Inner shadows
        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO,
                GlStateManager.DestFactor.ONE);
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        int shadowLength = 4;
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(this.x0, this.y0 + shadowLength, 0.0).color(0, 0, 0, 0).endVertex();
        bufferBuilder.vertex(this.x1, this.y0 + shadowLength, 0.0).color(0, 0, 0, 0).endVertex();
        bufferBuilder.vertex(this.x1, this.y0, 0.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.x0, this.y0, 0.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.x0, this.y1, 0.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.x1, this.y1, 0.0).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(this.x1, this.y1 - shadowLength, 0.0).color(0, 0, 0, 0).endVertex();
        bufferBuilder.vertex(this.x0, this.y1 - shadowLength, 0.0).color(0, 0, 0, 0).endVertex();
        tesselator.end();

        // todo: make scrollbar draggable
        // scrollbar
        int o = this.getMaxScroll();
        if (o > 0) {
            int scrollbarStart = x0 + this.width - 6;
            int scrollbarEnd = x0 + this.width;
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            int m = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
            m = Mth.clamp(m, 32, this.y1 - this.y0 - 8);
            int n = (int) this.getScrollAmount() * (this.y1 - this.y0 - m) / o + this.y0;
            if (n < this.y0) {
                n = this.y0;
            }
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.vertex(scrollbarStart, this.y1, 0.0).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(scrollbarEnd, this.y1, 0.0).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(scrollbarEnd, this.y0, 0.0).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(scrollbarStart, this.y0, 0.0).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(scrollbarStart, n + m, 0.0).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(scrollbarEnd, n + m, 0.0).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(scrollbarEnd, n, 0.0).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(scrollbarStart, n, 0.0).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(scrollbarStart, n + m - 1, 0.0).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(scrollbarEnd - 1, n + m - 1, 0.0).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(scrollbarEnd - 1, n, 0.0).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(scrollbarStart, n, 0.0).color(192, 192, 192, 255).endVertex();
            tesselator.end();
        }

        this.renderDecorations(poseStack, mouseX, mouseY);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderList(@NotNull PoseStack poseStack, int x, int y, int mouseX, int mouseY, float partialTick) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        // for each entry
        for (int i = 0; i < this.getItemCount(); ++i) {
            int entryTop = this.getRowTop(i);
            int entryBottom = this.getRowBottom(i);

            // if entry is not visible, skip
            if (entryBottom >= this.y0 && entryTop <= this.y1) {
                int height = y + i * this.itemHeight + this.headerHeight;

                int contentHeight = this.itemHeight - 4;

                BridgeEntry entry = this.getEntry(i);
                int rowWidth = this.getRowWidth();

                // selection box
                if (this.isSelectedItem(i)) {
                    int selectionBoxX0 = this.x0 + this.width / 2 - rowWidth / 2;
                    int scrollBarWidth = this.getMaxScroll() > 0 ? 6 : -1;
                    int selectionBoxX1 = this.x0 + this.width / 2 + rowWidth / 2 - scrollBarWidth;
                    RenderSystem.disableTexture();
                    RenderSystem.setShader(GameRenderer::getPositionShader);
                    float f = this.isFocused() ? 1.0F : 0.5F;
                    RenderSystem.setShaderColor(f, f, f, 1.0F);
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferBuilder.vertex(selectionBoxX0, height + contentHeight + 2, 0.0).endVertex();
                    bufferBuilder.vertex(selectionBoxX1, height + contentHeight + 2, 0.0).endVertex();
                    bufferBuilder.vertex(selectionBoxX1, height - 2, 0.0).endVertex();
                    bufferBuilder.vertex(selectionBoxX0, height - 2, 0.0).endVertex();
                    tesselator.end();
                    RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferBuilder.vertex(selectionBoxX0 + 1, height + contentHeight + 1, 0.0).endVertex();
                    bufferBuilder.vertex(selectionBoxX1 - 1, height + contentHeight + 1, 0.0).endVertex();
                    bufferBuilder.vertex(selectionBoxX1 - 1, height - 1, 0.0).endVertex();
                    bufferBuilder.vertex(selectionBoxX0 + 1, height - 1, 0.0).endVertex();
                    tesselator.end();
                    RenderSystem.enableTexture();
                }

                entry.render(poseStack,
                        i,
                        entryTop,
                        this.getRowLeft(),
                        rowWidth,
                        contentHeight,
                        mouseX,
                        mouseY,
                        Objects.equals(this.hovered, entry),
                        partialTick);
            }
        }

    }

    private int getRowBottom(int index) {
        return this.getRowTop(index) + this.itemHeight;
    }

    @Override
    public int getRowWidth() {
        return this.width - 8;
    }

    @Override
    protected boolean isFocused() {
        return parent.getFocused() == this;
    }

    @Override
    protected boolean isSelectedItem(int index) {
        return this.getSelected() != null && Objects.equals(this.getSelected().getBridge()
                                                                .getBridgeId(), this.children().get(index).getBridge()
                                                                                    .getBridgeId());
    }

    public static class BridgeEntry extends ObjectSelectionList.Entry<BridgeEntry> {
        private final Minecraft minecraft = Minecraft.getInstance();
        private final @Getter HueBridge bridge;
        private final LightSelectionScreen parentScreen;
        private final ResourceLocation BRIDGE_ICON = new ResourceLocation(McHue.MOD_ID, "textures/gui/bridge_icon.png");
        private boolean connected = false;

        public BridgeEntry(HueBridge bridge, LightSelectionScreen parentScreen) {
            this.bridge = bridge;
            this.parentScreen = parentScreen;
        }

        @Override
        public Component getNarration() {
            //todo: proper accessibility
            return Component.literal("Bridge");
        }

        @Override
        public void render(@NotNull PoseStack poseStack, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            int ICON_SIZE = 32;

            // icon
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, BRIDGE_ICON);
            RenderSystem.enableBlend();
            blit(poseStack, x, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            RenderSystem.disableBlend();

            // title
            Font font = this.minecraft.font;
            Component name = Component.literal("Bridge " + (index + 1));
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
                fill(poseStack, labelX + 1, y - 1, labelX + labelWidth, y, outlineColor);
                fill(poseStack, labelX, y, labelX + 1, y + Minecraft.getInstance().font.lineHeight, outlineColor);
                fill(poseStack, labelX + 1, y + 1 + Minecraft.getInstance().font.lineHeight - 1, labelX + labelWidth, y + Minecraft.getInstance().font.lineHeight + 1, outlineColor);
                fill(poseStack, labelX + labelWidth, y, labelX + labelWidth + 1, y + Minecraft.getInstance().font.lineHeight, outlineColor);
                //background
                fill(poseStack, labelX + 1, y, labelX + labelWidth, y + Minecraft.getInstance().font.lineHeight, backgroundColor);
                //text
                Minecraft.getInstance().font.draw(poseStack, labelContent, (labelX + 1 + (labelWidth - Minecraft.getInstance().font.width(labelContent)) / 2.0F), y + 1, textColor);
            }

            // description
            String summary = "IP: " + bridge.getBridgeIp() + System.lineSeparator() + "ID: " + bridge.getBridgeId();
            DrawingUtil.drawStrings(this.minecraft, poseStack, summary, x + 7 + ICON_SIZE, y + minecraft.font.lineHeight + 2, rowWidth - 7, 2, 0x808080);
        }

        @Override
        public boolean mouseClicked(double v, double v1, int i) {
            parentScreen.setSelectedBridgeEntry(this);
            return true;
        }

        public boolean isConnected() {
            return this.connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }
    }
}
