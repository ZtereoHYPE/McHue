package codes.ztereohype.mchue.gui;

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

/*
Okay so GUI and UX planning:
- By default, on launch, the mod loads and connects to the main bridge that will be decided as such by the SelectedBridgeId
  A SMALL TEST HAS TO BE RAN, otherwise the bridge will be disconnected
    - A lack of SelectedBridgeId will cause the user to be directly taken to a [looking for bridge] screen with the scan already started and a list showing asap
- Opening the screen will perform a network scan show the user a list of with on top the connected bridge (with a [selected] tag) and the others below
- Pressing Locate Bridge(s) will add to the list newly found bridges a [new] icon/tag
- doubleclicking on any of the bridges of the list will actually connect to the bridge, wether it's new and thus needs
  to go thru all of the connection steps, or it's already known
    - After each connection A SMALL TEST HAS TO BE RAN, otherwise the bridge will be disconnected
- The user will be prompted to connect with a toast on main menu or something when no main bridge is selected.
- will be half bridge (with on top a separated box for the connected one) list half lights list
- Clicking on the lights selects them (blinking them) and clicking again unselects them.
- Buttons on the bottom will be Re-Scan, Connect, Disconnect, Back. --> Back turns into Done when one or more working lights are selected!
- on top of each lists a short instruction will be provided (click to select the bridge) (click on one or more lights to connect or disconnect them)
 */


@Environment(value = EnvType.CLIENT)
public class ConfigurationScreen extends Screen {
    public final String title = "McHue configuration screen";
    private final Screen lastScreen;
    public BridgeConnectionScreen bridgeConnectionScreen;
    private BridgeSelectionList bridgeSelectionList;
    private LightSelectionList lightSelectionList;
    private BridgeEntry selectedBridgeEntry;

    public ConfigurationScreen(Screen lastScreen) {
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

        // todo: maybe do something with uh um yeah no this is very fucked shittttt
        if (McHue.ACTIVE_BRIDGE != null) {
            bridgeSelectionList.children().add(new BridgeEntry(McHue.ACTIVE_BRIDGE, bridgeSelectionList));
        }

        for (HueBridge bridge : BridgeManager.localBridges) {
            if (McHue.ACTIVE_BRIDGE != null && bridge.getBridgeIp().equals(McHue.ACTIVE_BRIDGE.getBridgeIp())) continue;
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
            McHue.ACTIVE_BRIDGE = selectedBridgeEntry.getBridge();
            selectedBridgeEntry.setConnected(true);
        } else {
            this.bridgeConnectionScreen = new BridgeConnectionScreen(this, selectedBridgeEntry);
            minecraft.setScreen(bridgeConnectionScreen);
        }
    }

    private void disconnectBridge() {
        //todo: find a way to unify all of this bullshit im sick and tired of losing it all around
        // also, this is broken and doesnt work
        selectedBridgeEntry.setConnected(false);
        setSelectedBridgeEntry(null);
        McHue.ACTIVE_BRIDGE = null;
    }

    public void setSelectedBridgeEntry(BridgeEntry entry) {
        this.selectedBridgeEntry = entry;
        this.lightSelectionList.setSelectedBridge(entry.getBridge());
    }
}
