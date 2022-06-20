package codes.ztereohype.mchue;

import codes.ztereohype.mchue.config.BridgeProperties;
import codes.ztereohype.mchue.config.Config;
import codes.ztereohype.mchue.config.ModProperties;
import codes.ztereohype.mchue.devices.BridgeManager;
import codes.ztereohype.mchue.devices.HueBridge;
import codes.ztereohype.mchue.gui.screens.LightSelectionScreen;
import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class McHue implements ClientModInitializer {
    public static final String MOD_ID = "mchue";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final BridgeManager BRIDGE_MANAGER = new BridgeManager();

    public static HueBridge activeBridge;
    public static LightSelectionScreen settingsScreen;

    private String toastTitle;
    private @Getter static McHue initialisedInstance;

    public static final Config BRIDGE_DATA = new Config(Paths.get("./.mchue/bridge_data.config"),
            "McHue config containing sensitive data about the bridge. WARNING: DO NOT SHARE THIS UNDER ANY CIRCUMSTANCES",
            Map.of(BridgeProperties.BRIDGE_ID.getSettingName(), BridgeProperties.BRIDGE_ID.getDefaultValue(),
                    BridgeProperties.BRIDGE_IP.getSettingName(), BridgeProperties.BRIDGE_IP.getDefaultValue(),
                    BridgeProperties.DEVICE_INDENTIFIER.getSettingName(), BridgeProperties.DEVICE_INDENTIFIER.getDefaultValue(),
                    BridgeProperties.USERNAME.getSettingName(), BridgeProperties.USERNAME.getDefaultValue(),
                    BridgeProperties.CLIENT_KEY.getSettingName(), BridgeProperties.CLIENT_KEY.getDefaultValue(),
                    BridgeProperties.CONNECTED_LIGHTS.getSettingName(), BridgeProperties.CONNECTED_LIGHTS.getDefaultValue()));

    public static final Config SETTINGS_CONFIG = new Config(Paths.get("./config/mchue.config"),
            "McHue config containing general mod settings",
            Map.of(ModProperties.IS_ACTIVE.getSettingName(), ModProperties.IS_ACTIVE.getDefaultValue()));

    @Override
    public void onInitializeClient() {
        initialisedInstance = this;
        try {
            BRIDGE_DATA.initialise();
            SETTINGS_CONFIG.initialise();
        } catch (IOException e) {
            LOGGER.log(Level.FATAL, "Could not read or write McHue config file.");
            // TODO: crash the game? idk how to handle exceptions yet
            e.printStackTrace();
        }

        setupBridgeConnection();
    }

    private void setupBridgeConnection() {
        boolean savedBridgeFoundLocally = BridgeManager.getLocalBridges(false).stream()
                                                       .anyMatch(b -> b.getBridgeId()
                                                                       .equals(BRIDGE_DATA.getProperty(BridgeProperties.BRIDGE_ID)));

        if (savedBridgeFoundLocally) {
            String id = BRIDGE_DATA.getProperty(BridgeProperties.BRIDGE_ID);
            String ip = BRIDGE_DATA.getProperty(BridgeProperties.BRIDGE_IP);
            activeBridge = new HueBridge(id, ip);

            BRIDGE_MANAGER.completeBridge(activeBridge);
        }

        toastTitle = !savedBridgeFoundLocally ? "Could not find Bridge." : "Set up the Hue Bridge.";
    }

    //todo: fix this bs -_-
    public void displayToast() {
        if (toastTitle == null) return;
//        Minecraft.getInstance().
        Minecraft.getInstance().getToasts()
                 .addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE, new TextComponent(toastTitle), new TextComponent("Go to the McHue settings.")));
    }

    public boolean isEntertainmentMode() {
        return Boolean.getBoolean(SETTINGS_CONFIG.getProperty(ModProperties.ENTERTAINMENT_ZONES));
    }
}
