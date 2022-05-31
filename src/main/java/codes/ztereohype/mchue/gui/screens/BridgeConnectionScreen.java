package codes.ztereohype.mchue.gui.screens;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.devices.BridgeManager;
import codes.ztereohype.mchue.devices.HueBridge;
import codes.ztereohype.mchue.gui.widget.entries.BridgeEntry;
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

    private @Setter String subtitle = "Press the bridge button.";
    private @Setter String countdown = "60s remaining...";
    private ResourceLocation currentImage = BRIDGE_CONNECTING_IMAGE;

    private Button tryAgainButton;
    private Button backButton;

    private boolean allowConnectingOnInit = true;

    public BridgeConnectionScreen(Screen lastScreen, BridgeEntry connectingBridgeEntry) {
        super(Component.nullToEmpty("Connect to the Bridge"));
        this.lastScreen = lastScreen;
        this.connectingBridgeEntry = connectingBridgeEntry;
        this.connectingBridge = connectingBridgeEntry.getBridge();
    }

    public void init() {
        assert this.minecraft != null;
        super.init();

        //todo: correct sizes and make button gray while connecting
        tryAgainButton = this.addRenderableWidget(new Button(this.width / 2 + 4, this.height - 28, 150, 20, new TextComponent("Try Again"), (button) -> {
            subtitle = "Press the bridge button.";
            startConnection();
        }));

        tryAgainButton.active = false;

        backButton = this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 28, 150, 20, new TextComponent("Back"), (button) -> {
            //todo: make this less ugly of a cancel ????
            BridgeManager.cancelConnection();
            this.minecraft.setScreen(lastScreen);
        }));

        if (allowConnectingOnInit) {
            startConnection();
            allowConnectingOnInit = false;
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
        BridgeManager.startInitialBridgeConnection(connectingBridge);
    }

    public void connectionComplete() {
        if (connectingBridge.isComplete()) {
            currentImage = BRIDGE_SUCCESS_IMAGE;
            McHue.activeBridge = connectingBridge;
            backButton.setMessage(new TextComponent("Done"));
        } else {
            currentImage = BRIDGE_FAILED_IMAGE;
            tryAgainButton.active = true;
        }
    }
}
