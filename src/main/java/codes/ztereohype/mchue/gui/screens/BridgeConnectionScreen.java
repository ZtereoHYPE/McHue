package codes.ztereohype.mchue.gui.screens;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.config.BridgeProperties;
import codes.ztereohype.mchue.devices.HueBridge;
import codes.ztereohype.mchue.devices.interfaces.BridgeConnectionUpdate;
import codes.ztereohype.mchue.gui.widget.entries.BridgeEntry;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

//todo: changing this into a popup window like 1.8 reset custom world settings would be nice
public class BridgeConnectionScreen extends Screen {
    public final String TITLE = "Connect to the Bridge";
    public final HueBridge connectingBridge;
    public final BridgeEntry connectingBridgeEntry;
    private final Screen lastScreen;

    private final int IMAGE_SIZE = 140;
    private final ResourceLocation BRIDGE_CONNECTING_IMAGE = new ResourceLocation(McHue.MOD_ID, "textures/gui/bridge_press_button.png");
    private final ResourceLocation BRIDGE_SUCCESS_IMAGE = new ResourceLocation(McHue.MOD_ID, "textures/gui/bridge_connection_success.png");
    private final ResourceLocation BRIDGE_FAILED_IMAGE = new ResourceLocation(McHue.MOD_ID, "textures/gui/bridge_connection_failed.png");

    private @Setter String subtitle = "Press the bridge Pushlink button.";
    private @Setter String countdown = "60s remaining...";
    private ResourceLocation currentImage = BRIDGE_CONNECTING_IMAGE;

    private Button tryAgainButton;
    private Button backButton;

    private boolean hasStartedConnection = false;

    public BridgeConnectionScreen(Screen lastScreen, BridgeEntry connectingBridgeEntry) {
        super(Component.nullToEmpty("Connect to the Bridge"));
        this.lastScreen = lastScreen;
        this.connectingBridgeEntry = connectingBridgeEntry;
        this.connectingBridge = connectingBridgeEntry.getBridge();
    }

    public void init() {
        assert this.minecraft != null;
        super.init();

        tryAgainButton = this.addRenderableWidget(new Button(this.width / 2 + 4, this.height - 28, 150, 20, new TextComponent("Try Again"), (button) -> {
            setSubtitle("Press the bridge button.");
            startConnection();
        }));

        tryAgainButton.active = false;

        backButton = this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 28, 150, 20, new TextComponent("Back"), (button) -> {
            McHue.BRIDGE_MANAGER.cancelConnection();
            this.minecraft.setScreen(lastScreen);
        }));

        if (!hasStartedConnection) {
            startConnection();
            hasStartedConnection = true;
        }
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(0);

        super.render(poseStack, mouseX, mouseY, delta);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, currentImage);
        RenderSystem.enableBlend();
        GuiComponent.blit(poseStack, this.width / 2 - IMAGE_SIZE / 2, this.height / 2 - IMAGE_SIZE / 2 + 6, 0.0F, 0.0F, IMAGE_SIZE, IMAGE_SIZE, IMAGE_SIZE, IMAGE_SIZE);
        RenderSystem.disableBlend();

        drawCenteredString(poseStack, this.font, this.TITLE, this.width / 2, 8, 16777215);
        drawCenteredString(poseStack, this.font, this.subtitle, this.width / 2, 24, 16777215);
        drawCenteredString(poseStack, this.font, this.countdown, this.width / 2, 34, 16777215);
    }

    private void startConnection() {
        currentImage = BRIDGE_CONNECTING_IMAGE;
        McHue.BRIDGE_MANAGER.completeBridge(connectingBridge, this::connectionUpdate);
    }

    private void connectionUpdate(BridgeConnectionUpdate updateInfo) {
        switch (updateInfo.response()) {
            case SUCCESS -> {
                // this should be an unreachable state
                if (!connectingBridge.isComplete()) Blaze3D.youJustLostTheGame();

                setSubtitle("Connection completed with Success!");
                setCountdown("You may go back to the previous screen.");

                currentImage = BRIDGE_SUCCESS_IMAGE;
                McHue.activeBridge = connectingBridge;
                backButton.setMessage(new TextComponent("Done"));

                McHue.BRIDGE_DATA.setProperty(BridgeProperties.BRIDGE_ID, connectingBridge.getBridgeId());
                McHue.BRIDGE_DATA.setProperty(BridgeProperties.BRIDGE_IP, connectingBridge.getBridgeIp());
                McHue.BRIDGE_DATA.setProperty(BridgeProperties.DEVICE_INDENTIFIER, connectingBridge.getDeviceId());
                McHue.BRIDGE_DATA.setProperty(BridgeProperties.USERNAME, connectingBridge.getUsername());
                McHue.BRIDGE_DATA.setProperty(BridgeProperties.CLIENT_KEY, connectingBridge.getClientKey());
            }
            case PRESS_BUTTON -> setCountdown(updateInfo.timeLeft() + "s remaining...");

            case TIME_UP -> {
                setSubtitle("The button was not pressed in 60 seconds.");
                setCountdown("You can press Try Again to... try again.");

                currentImage = BRIDGE_FAILED_IMAGE;
                tryAgainButton.active = true;
            }
            case FAILURE -> {
                setSubtitle(updateInfo.errorMessage());
                setCountdown("");

                currentImage = BRIDGE_FAILED_IMAGE;
                tryAgainButton.active = true;
            }
        }
    }
}
