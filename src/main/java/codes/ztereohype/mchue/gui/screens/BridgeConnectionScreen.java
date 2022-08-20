package codes.ztereohype.mchue.gui.screens;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.config.BridgeProperties;
import codes.ztereohype.mchue.devices.HueBridge;
import codes.ztereohype.mchue.devices.interfaces.BridgeConnectionHandler;
import codes.ztereohype.mchue.gui.widget.BridgeSelectionList;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BridgeConnectionScreen extends Screen {
    public final HueBridge connectingBridge;
    public final BridgeSelectionList.BridgeEntry connectingBridgeEntry;
    private final Screen lastScreen;

    private static final int IMAGE_SIZE = 192;
    private static final ResourceLocation BRIDGE_IMAGE = new ResourceLocation(McHue.MOD_ID, "textures/gui/bridge_large.png");
    private static final ResourceLocation CHECKMARK = new ResourceLocation(McHue.MOD_ID, "textures/gui/checkmark.png");
    private static final ResourceLocation CROSS = new ResourceLocation(McHue.MOD_ID, "textures/gui/cross.png");
    private static final ResourceLocation ARROW = new ResourceLocation(McHue.MOD_ID, "textures/gui/arrow.png");

    private @Setter String subtitle = "Press the bridge Pushlink button.";
    private @Setter String countdown = "60s remaining...";
    private ResourceLocation currentDecoration = ARROW;
    private byte blink;

    private Button tryAgainButton;
    private Button backButton;

    private boolean hasStartedConnection = false;

    public BridgeConnectionScreen(Screen lastScreen, BridgeSelectionList.BridgeEntry connectingBridgeEntry) {
        super(Component.literal("Connect to the Bridge"));
        this.lastScreen = lastScreen;
        this.connectingBridgeEntry = connectingBridgeEntry;
        this.connectingBridge = connectingBridgeEntry.getBridge();
    }

    public void init() {
        assert this.minecraft != null;
        super.init();

        tryAgainButton = this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 28, 150, 20, Component.literal("Try Again"), (button) -> {
            setSubtitle("Press the bridge button.");
            startConnection();
        }));

        tryAgainButton.active = false;

        backButton = this.addRenderableWidget(new Button(this.width / 2 + 4, this.height - 28, 150, 20, Component.literal("Back"), (button) -> {
            McHue.BRIDGE_MANAGER.cancelConnection();
            this.minecraft.setScreen(lastScreen);
        }));

        if (!hasStartedConnection) {
            startConnection();
            hasStartedConnection = true;
        }
    }

    @Override
    public void tick() {
        super.tick();
        blink+=10;
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(0);

        super.render(poseStack, mouseX, mouseY, delta);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BRIDGE_IMAGE);
        RenderSystem.enableBlend();
        GuiComponent.blit(poseStack, this.width / 2 - IMAGE_SIZE / 2, this.height / 2 - IMAGE_SIZE / 2 + 6, 0.0F, 0.0F, IMAGE_SIZE, IMAGE_SIZE, IMAGE_SIZE, IMAGE_SIZE);
        RenderSystem.disableBlend();

        float uOffset = 0;
        if (currentDecoration == ARROW) {
            uOffset = blink > 0 ? 40F : 55F;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, currentDecoration);
        RenderSystem.enableBlend();
        GuiComponent.blit(poseStack, this.width / 2 - IMAGE_SIZE / 2, this.height / 2 - IMAGE_SIZE / 2 + 6, uOffset, 1.0F, IMAGE_SIZE, IMAGE_SIZE, IMAGE_SIZE, IMAGE_SIZE);
        RenderSystem.disableBlend();

        drawCenteredString(poseStack, this.font, getTitle(), this.width / 2, 8, 16777215);
        drawCenteredString(poseStack, this.font, this.subtitle, this.width / 2, 24, 16777215);
        drawCenteredString(poseStack, this.font, this.countdown, this.width / 2, 34, 16777215);
    }

    private void startConnection() {
        currentDecoration = ARROW;
        McHue.BRIDGE_MANAGER.completeBridge(connectingBridge, new ScreenConnectionHandler());
    }

    private class ScreenConnectionHandler implements BridgeConnectionHandler {
        @Override
        public void success(String username) {
            // this should be an unreachable state
            if (!connectingBridge.isComplete()) Blaze3D.youJustLostTheGame();

            setSubtitle("Connection completed with Success!");
            setCountdown("You may go back to the previous screen.");

            currentDecoration = CHECKMARK;
            McHue.activeBridge = connectingBridge;
            backButton.setMessage(Component.literal("Done"));

            McHue.BRIDGE_DATA.setProperty(BridgeProperties.BRIDGE_ID, connectingBridge.getBridgeId());
            McHue.BRIDGE_DATA.setProperty(BridgeProperties.BRIDGE_IP, connectingBridge.getBridgeIp());
            McHue.BRIDGE_DATA.setProperty(BridgeProperties.DEVICE_INDENTIFIER, connectingBridge.getDeviceId());
            McHue.BRIDGE_DATA.setProperty(BridgeProperties.USERNAME, connectingBridge.getUsername());
            McHue.BRIDGE_DATA.setProperty(BridgeProperties.CLIENT_KEY, connectingBridge.getClientKey());
        }

        @Override
        public void pressButton(int timeLeft) {
            setCountdown(timeLeft + "s remaining...");
        }

        @Override
        public void timeUp() {
            setSubtitle("The button was not pressed in 60 seconds.");
            setCountdown("You can press Try Again to... try again.");

            currentDecoration = CROSS;
            tryAgainButton.active = true;
        }

        @Override
        public void failure(String errorMessage) {
            setSubtitle(errorMessage);
            setCountdown("");

            currentDecoration = CROSS;
            tryAgainButton.active = true;
        }
    }
}
