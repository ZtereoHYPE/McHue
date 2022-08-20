package codes.ztereohype.mchue.gui.widget;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.config.BridgeProperties;
import codes.ztereohype.mchue.devices.HueBridge;
import codes.ztereohype.mchue.devices.HueLight;
import codes.ztereohype.mchue.devices.interfaces.LightState;
import codes.ztereohype.mchue.gui.screens.LightSelectionScreen;
import codes.ztereohype.mchue.util.DrawingUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class LightSelectionList extends ObjectSelectionList<LightSelectionList.LightEntry> {
    private static final LightState BLINK_COLOUR = new LightState(32, 255, 128, true);
    private final LightSelectionScreen parent;
    private HueBridge selectedBridge;

    private @Setter String emptyMessage = "Select a bridge to view its lights.";

    public LightSelectionList(Minecraft minecraft, int x0, int x1, int y0, int y1, int itemHeight, LightSelectionScreen parent) {
        super(minecraft, x1 - x0, y1 - y0, y0, y1, itemHeight);
        this.x0 = x0;
        this.x1 = x1;
        this.parent = parent;
    }

    @Override
    public void setSelected(LightEntry entry) {
        if (entry == null) return; // ???

        boolean selected = !selectedBridge.getActiveLights().contains(entry.getLight().getId());

        if (selected) {
            LightState previousColour = entry.getLight().getState();
            entry.getLight().setColour(BLINK_COLOUR);
            entry.getLight().setColour(previousColour);
        }

        selectedBridge.setActiveLight(entry.getLight().getId(), selected);

        String[] lightIds = selectedBridge.getActiveLights().toArray(String[]::new);

        McHue.BRIDGE_DATA.setPropertyArray(BridgeProperties.CONNECTED_LIGHTS, lightIds);
    }

    @Override
    public boolean isSelectedItem(int index) {
        String lightId = this.children().get(index).getLight().getId();

        return selectedBridge.getActiveLights().contains(lightId);
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

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
        int z = -100;
        int bottomDistance = this.parent.height - this.y1;
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        //top?
        bufferBuilder.vertex(this.x0, this.y0, z)
                     .uv(this.x0 / f, this.y0 / f)
                     .color(64, 64, 64, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x0 + this.width, this.y0, z)
                     .uv((this.x0 + this.width) / f, this.y0 / f)
                     .color(64, 64, 64, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x0 + this.width, 0.0, z)
                     .uv((this.x0 + this.width) / f, 0.0F)
                     .color(64, 64, 64, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x0, 0.0, z)
                     .uv(this.x0 / f, 0.0F)
                     .color(64, 64, 64, 255)
                     .endVertex();

        //bottom?
        bufferBuilder.vertex(this.x0, this.y1 + bottomDistance, z)
                     .uv(this.x0 / f, (this.y1 + bottomDistance) / f)
                     .color(64, 64, 64, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x0 + this.width, this.y1 + bottomDistance, z)
                     .uv((this.x0 + this.width) / f, (this.y1 + bottomDistance) / f)
                     .color(64, 64, 64, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x0 + this.width, this.y1, z)
                     .uv((this.x0 + this.width) / f, this.y1 / f)
                     .color(64, 64, 64, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x0, this.y1, z)
                     .uv(this.x0 / f, this.y1 / f)
                     .color(64, 64, 64, 255)
                     .endVertex();
        tesselator.end();

        // Inner shadows
        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        int shadowLength = 4;
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(this.x0, this.y0 + shadowLength, 0.0)
                     .color(0, 0, 0, 0)
                     .endVertex();
        bufferBuilder.vertex(this.x1, this.y0 + shadowLength, 0.0)
                     .color(0, 0, 0, 0)
                     .endVertex();
        bufferBuilder.vertex(this.x1, this.y0, 0.0)
                     .color(0, 0, 0, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x0, this.y0, 0.0)
                     .color(0, 0, 0, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x0, this.y1, 0.0)
                     .color(0, 0, 0, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x1, this.y1, 0.0)
                     .color(0, 0, 0, 255)
                     .endVertex();
        bufferBuilder.vertex(this.x1, this.y1 - shadowLength, 0.0)
                     .color(0, 0, 0, 0)
                     .endVertex();
        bufferBuilder.vertex(this.x0, this.y1 - shadowLength, 0.0)
                     .color(0, 0, 0, 0)
                     .endVertex();
        tesselator.end();


        // scrollbar
        int overflow = this.getMaxScroll();
        if (overflow > 0) {
            int scrollbarStart = x0 + this.width - 6;
            int scrollbarEnd = x0 + this.width;
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            int m = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
            m = Mth.clamp(m, 32, this.y1 - this.y0 - 8);
            int n = (int) this.getScrollAmount() * (this.y1 - this.y0 - m) / overflow + this.y0;
            if (n < this.y0) {
                n = this.y0;
            }
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.vertex(scrollbarStart, this.y1, 0.0)
                         .color(0, 0, 0, 255)
                         .endVertex();
            bufferBuilder.vertex(scrollbarEnd, this.y1, 0.0)
                         .color(0, 0, 0, 255)
                         .endVertex();
            bufferBuilder.vertex(scrollbarEnd, this.y0, 0.0)
                         .color(0, 0, 0, 255)
                         .endVertex();
            bufferBuilder.vertex(scrollbarStart, this.y0, 0.0)
                         .color(0, 0, 0, 255)
                         .endVertex();
            bufferBuilder.vertex(scrollbarStart, n + m, 0.0)
                         .color(128, 128, 128, 255)
                         .endVertex();
            bufferBuilder.vertex(scrollbarEnd, n + m, 0.0)
                         .color(128, 128, 128, 255)
                         .endVertex();
            bufferBuilder.vertex(scrollbarEnd, n, 0.0)
                         .color(128, 128, 128, 255)
                         .endVertex();
            bufferBuilder.vertex(scrollbarStart, n, 0.0)
                         .color(128, 128, 128, 255)
                         .endVertex();
            bufferBuilder.vertex(scrollbarStart, n + m - 1, 0.0)
                         .color(192, 192, 192, 255)
                         .endVertex();
            bufferBuilder.vertex(scrollbarEnd - 1, n + m - 1, 0.0)
                         .color(192, 192, 192, 255)
                         .endVertex();
            bufferBuilder.vertex(scrollbarEnd - 1, n, 0.0)
                         .color(192, 192, 192, 255)
                         .endVertex();
            bufferBuilder.vertex(scrollbarStart, n, 0.0)
                         .color(192, 192, 192, 255)
                         .endVertex();
            tesselator.end();
        }

        this.renderDecorations(poseStack, mouseX, mouseY);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderList(@NotNull PoseStack poseStack, int x, int y, int mouseX, int mouseY, float partialTick) {
        int listLength = this.getItemCount();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        // for each entry
        for (int i = 0; i < listLength; ++i) {
            int entryTop = this.getRowTop(i);
            int entryBottom = this.getRowBottom(i);

            // if entry is not visible, skip
            if (entryBottom >= this.y0 && entryTop <= this.y1) {
                int height = y + i * this.itemHeight + this.headerHeight;

                int contentHeight = this.itemHeight - 4;

                LightEntry entry = this.getEntry(i);
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
                    bufferBuilder.vertex(selectionBoxX0, height + contentHeight + 2, 0.0)
                                 .endVertex();
                    bufferBuilder.vertex(selectionBoxX1, height + contentHeight + 2, 0.0)
                                 .endVertex();
                    bufferBuilder.vertex(selectionBoxX1, height - 2, 0.0)
                                 .endVertex();
                    bufferBuilder.vertex(selectionBoxX0, height - 2, 0.0)
                                 .endVertex();
                    tesselator.end();
                    RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferBuilder.vertex(selectionBoxX0 + 1, height + contentHeight + 1, 0.0)
                                 .endVertex();
                    bufferBuilder.vertex(selectionBoxX1 - 1, height + contentHeight + 1, 0.0)
                                 .endVertex();
                    bufferBuilder.vertex(selectionBoxX1 - 1, height - 1, 0.0)
                                 .endVertex();
                    bufferBuilder.vertex(selectionBoxX0 + 1, height - 1, 0.0)
                                 .endVertex();
                    tesselator.end();
                    RenderSystem.enableTexture();
                }

                entry.render(poseStack, i, entryTop, this.getRowLeft(), rowWidth, contentHeight, mouseX, mouseY, false, partialTick);
            }
        }
    }

    @Override
    protected void renderDecorations(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        //error message
        //todo: fix this horrible one-liner
        minecraft.font.draw(poseStack, emptyMessage, x0 + (width - minecraft.font.width(emptyMessage)) / 2.0F, y0 + (height - minecraft.font.lineHeight) / 2.0F, 0x808080);
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

    //todo: make this change the icons
    public void setSelectedBridge(HueBridge bridge) {
        this.selectedBridge = bridge;

        if (bridge == null) {
            this.clearEntries();
            return;
        }

        if (!bridge.isComplete()) {
            emptyMessage = "Press \"Connect\" to finish connecting.";
            return;
        }

        if (!bridge.locateLights()) {
            emptyMessage = "There was a problem connecting...";
            return;
        }

        emptyMessage = "";
        this.clearEntries();
        for (HueLight light : bridge.bridgeLights.values()) {
            LightEntry entry = new LightEntry(light, this);
            this.addEntry(entry);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            LightEntry entry = this.getEntryAtPosition(mouseX, mouseY);
            if (entry != null) {
                if (entry.mouseClicked(mouseX, mouseY, button)) {
                    this.setSelected(entry);

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + 124 + x0;
    }

    public static class LightEntry extends Entry<LightEntry> {
        private final Minecraft minecraft = Minecraft.getInstance();
        private final @Getter HueLight light;
        private final ResourceLocation LIGHT_ICON = new ResourceLocation(McHue.MOD_ID, "textures/gui/lightbulb_icon.png");
        private final LightSelectionList parent;

        public LightEntry(HueLight light, LightSelectionList parent) {
            this.light = light;
            this.parent = parent;
        }

        //todo
        @Override
        public Component getNarration() {
            return null;
        }

        @Override
        public void render(@NotNull PoseStack poseStack, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            int ICON_SIZE = 32;

            // icon
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, LIGHT_ICON);
            RenderSystem.enableBlend();
            blit(poseStack, x, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            RenderSystem.disableBlend();

            // title
            Font font = this.minecraft.font;
            Component name = Component.literal(light.getName());
            this.minecraft.font.draw(poseStack, Language.getInstance()
                                                        .getVisualOrder(name), x + 3 + ICON_SIZE, y + 1, 0xFFFFFF);

            // label
            if (parent.isSelectedItem(index)) {
                String labelContent = "Selected";
                int labelWidth = font.width(labelContent) + 6;
                int labelX = x + 5 + ICON_SIZE + font.width(name);
                int outlineColor = 0xff004499;
                int backgroundColor = 0xff122f61;
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
            String summary = "Index: " + light.getIndex() + System.lineSeparator() + "ID: " + light.getId();
            DrawingUtil.drawStrings(this.minecraft, poseStack, summary, x + 7 + ICON_SIZE, y + minecraft.font.lineHeight + 2, rowWidth - 7, 2, 0x808080);
        }

        public boolean mouseClicked(double v, double v1, int i) {
            return true;
        }
    }
}