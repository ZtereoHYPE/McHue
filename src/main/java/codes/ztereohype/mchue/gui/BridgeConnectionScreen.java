package codes.ztereohype.mchue.gui;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.devices.BridgeManager;
import codes.ztereohype.mchue.devices.HueBridge;
import codes.ztereohype.mchue.gui.widget.entries.BridgeEntry;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

//todo: changing this into a popup window like 1.8 reset custom world settings would be nice
public class BridgeConnectionScreen extends Screen {
    private final Screen lastScreen;
    private final int IMAGE_SIZE = 140;
    public final String title = "Connect to the Bridge";
    public final HueBridge connectingBridge;
    public final BridgeEntry connectingBridgeEntry;

    private Button tryAgainButton;
    private Button backButton;

    //todo: change the countdown to an active one
    public String message = "Press the bridge button... (you have 60s)";
    private final ResourceLocation bridgeImage = new ResourceLocation(McHue.MOD_ID, "textures/gui/bridgehd.png");

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
        tryAgainButton = this.addRenderableWidget(new Button(this.width / 2 - 156,
                                                             this.height - 28,
                                                             72,
                                                             20,
                                                             new TextComponent("Try Again"),
                                                             (button) -> {
                                                                 message = "Press the bridge button...";
                                                                 startConnection();
                                                             }));

        tryAgainButton.active = false;

        backButton = this.addRenderableWidget(new Button(this.width / 2 + 84,
                                                         this.height - 28,
                                                         72,
                                                         20,
                                                         new TextComponent("Back"),
                                                         (button) -> {
            //todo: make this less ugly of a cancel
            BridgeManager.cancelConnection();
            this.minecraft.setScreen(lastScreen);
        }));

        startConnection();

    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(0);

        super.render(poseStack, mouseX, mouseY, delta);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, bridgeImage);
        RenderSystem.enableBlend();
        GuiComponent.blit(poseStack, this.width/2 - IMAGE_SIZE/2, this.height/2 - IMAGE_SIZE/2, 0.0F, 0.0F, IMAGE_SIZE, IMAGE_SIZE, IMAGE_SIZE, IMAGE_SIZE);
        RenderSystem.disableBlend();

        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 16777215);
        drawCenteredString(poseStack, this.font, this.message, this.width / 2, 32, 16777215);
    }

    private void startConnection() {
        BridgeManager.startInitialBridgeConnection(connectingBridge);
    }

    public void setConnectionUpdate(String message) {
        this.message = message;
    }

    public void connectionComplete() {
        if (connectingBridge.isComplete()) {
            McHue.ACTIVE_BRIDGE = connectingBridge;
            backButton.setMessage(new TextComponent("Done"));
        }
        else tryAgainButton.active = true;
    }
}
