package codes.ztereohype.mchue.gui.screens;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.devices.BridgeManager;
import codes.ztereohype.mchue.devices.HueBridge;
import codes.ztereohype.mchue.gui.widget.BridgeSelectionList;
import codes.ztereohype.mchue.gui.widget.LightSelectionList;
import codes.ztereohype.mchue.gui.widget.entries.BridgeEntry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

@Environment(value = EnvType.CLIENT)
public class LightConfigurationScreen extends Screen {
    public final String title = "McHue configuration screen";
    private final Screen lastScreen;
    public BridgeConnectionScreen bridgeConnectionScreen;
    private BridgeSelectionList bridgeSelectionList;
    private LightSelectionList lightSelectionList;
    private BridgeEntry selectedBridgeEntry;

    public LightConfigurationScreen(Screen lastScreen) {
        super(Component.nullToEmpty("McHue configuration screen"));
        this.lastScreen = lastScreen;
    }

    public void init() {
        assert this.minecraft != null;
        super.init();

        bridgeSelectionList = this.addRenderableWidget(new BridgeSelectionList(this.minecraft, 0, this.width / 2 - 8, 50, this.height - 36, 36, this));
        lightSelectionList = this.addRenderableWidget(new LightSelectionList(this.minecraft, this.width / 2 + 8, this.width, 50, this.height - 36, 36, this));

        if (selectedBridgeEntry != null) {
            bridgeSelectionList.setSelected(selectedBridgeEntry);
            lightSelectionList.setSelectedBridge(selectedBridgeEntry.getBridge());
        }

        this.addRenderableWidget(new Button(this.width / 2 - 156,
                this.height - 28,
                72,
                20,
                new TextComponent("Scan Bridges"),
                button -> BridgeManager.scanBridges()));

        this.addRenderableWidget(new Button(this.width / 2 - 76,
                this.height - 28,
                72,
                20,
                new TextComponent("Connect"),
                button -> connectBridge()) {

            @Override
            public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                active = selectedBridgeEntry != null && !selectedBridgeEntry.isConnected();
                super.renderButton(poseStack, mouseX, mouseY, partialTick);
            }
        });

        this.addRenderableWidget(new Button(this.width / 2 + 4,
                this.height - 28,
                72,
                20,
                new TextComponent("Disconnect"),
                button -> disconnectBridge()) {

            @Override
            public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                active = selectedBridgeEntry != null && selectedBridgeEntry.isConnected();
                super.renderButton(poseStack, mouseX, mouseY, partialTick);
            }
        });

        this.addRenderableWidget(new Button(this.width / 2 + 84,
                this.height - 28,
                72,
                20,
                new TextComponent("Done"),
                (button) -> this.minecraft.setScreen(lastScreen)));

        // todo: maybe do something with uh um yeah no this is very bad
        if (McHue.activeBridge != null) {
            BridgeEntry entry = new BridgeEntry(McHue.activeBridge, bridgeSelectionList);
            bridgeSelectionList.children().add(entry);
            bridgeSelectionList.setSelected(entry);
            lightSelectionList.setSelectedBridge(entry.getBridge());
        }

        for (HueBridge bridge : BridgeManager.localBridges) {
            if (McHue.activeBridge != null && bridge.getBridgeIp().equals(McHue.activeBridge.getBridgeIp())) continue;
            bridgeSelectionList.children().add(new BridgeEntry(bridge, bridgeSelectionList));
        }
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(0);
        super.render(poseStack, mouseX, mouseY, delta);

        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 16777215);
        drawCenteredString(poseStack, this.font, "Connect the bridge.", this.width / 4, 36, 16777215);
        drawCenteredString(poseStack, this.font, "Select or unselect lights.", 3 * this.width / 4, 36, 16777215);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (bridgeSelectionList.isMouseOver(mouseX, mouseY)) {
            return this.bridgeSelectionList.mouseScrolled(mouseX, mouseY, amount);
        }
        if (lightSelectionList.isMouseOver(mouseX, mouseY)) {
            return this.lightSelectionList.mouseScrolled(mouseX, mouseY, amount);
        }
        return false;
    }

    private void connectBridge() {
        //todo: yeha
        if (selectedBridgeEntry.getBridge().passedConnectionTest) {
            McHue.activeBridge = selectedBridgeEntry.getBridge();
            selectedBridgeEntry.setConnected(true);
        } else {
            this.bridgeConnectionScreen = new BridgeConnectionScreen(this, selectedBridgeEntry);
            minecraft.setScreen(bridgeConnectionScreen);
        }
    }

    private void disconnectBridge() {
        //todo: find a way to unify all of this stuff im sick and tired of losing it all around
        // also, this is broken and doesnt work
        selectedBridgeEntry.setConnected(false);
        setSelectedBridgeEntry(null);
        McHue.activeBridge = null;
    }

    public void setSelectedBridgeEntry(BridgeEntry entry) {
        this.selectedBridgeEntry = entry;
        this.lightSelectionList.setSelectedBridge(entry.getBridge());
    }
}
