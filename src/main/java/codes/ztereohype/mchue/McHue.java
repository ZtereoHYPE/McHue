package codes.ztereohype.mchue;

import codes.ztereohype.mchue.config.BridgeProperties;
import codes.ztereohype.mchue.config.Config;
import codes.ztereohype.mchue.config.ModSettings;
import codes.ztereohype.mchue.devices.BridgeManager;
import codes.ztereohype.mchue.devices.HueBridge;
import codes.ztereohype.mchue.gui.ConfigurationScreen;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class McHue implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("mchue");
    public static final String MOD_ID = "mchue";

    public static HueBridge ACTIVE_BRIDGE;
    public static ConfigurationScreen settingsScreen;

    public static Config BRIDGE_DATA = new Config(Paths.get("./.mchue/bridge_data.config"),
            "McHue config containing sensitive data about the bridge. WARNING: DO NOT SHARE THIS UNDER ANY CIRCUMSTANCES",
            Map.of(BridgeProperties.BRIDGE_ID.getSettingName(), BridgeProperties.BRIDGE_ID.getDefaultValue(),
                    BridgeProperties.BRIDGE_IP.getSettingName(), BridgeProperties.BRIDGE_IP.getDefaultValue(),
                    BridgeProperties.DEVICE_INDENTIFIER.getSettingName(), BridgeProperties.DEVICE_INDENTIFIER.getDefaultValue(),
                    BridgeProperties.USERNAME.getSettingName(), BridgeProperties.USERNAME.getDefaultValue(),
                    BridgeProperties.CONNECTED_LIGHTS.getSettingName(), BridgeProperties.CONNECTED_LIGHTS.getDefaultValue()));

    public static Config SETTINGS_CONFIG = new Config(Paths.get("./config/mchue.config"),
            "McHue config containing general mod settings",
            Map.of(ModSettings.IS_ACTIVE.getSettingName(), ModSettings.IS_ACTIVE.getDefaultValue()));

    @Override
    public void onInitialize() {
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
        BridgeManager.scanBridges();

        boolean validSavedBridge = !(Objects.equals(BRIDGE_DATA.getProperty(BridgeProperties.BRIDGE_IP), "null")
                || Objects.equals(BRIDGE_DATA.getProperty(BridgeProperties.BRIDGE_ID), "null")
                || Objects.equals(BRIDGE_DATA.getProperty(BridgeProperties.USERNAME), "null")
                || Objects.equals(BRIDGE_DATA.getProperty(BridgeProperties.DEVICE_INDENTIFIER), "null"));

        Optional<HueBridge> savedBridgeFoundLocally = BridgeManager.localBridges.stream().filter(b -> b.getBridgeIp()
                                                                                                       .equals(BRIDGE_DATA.getProperty(BridgeProperties.BRIDGE_IP)))
                                                                                .findFirst();

        if (validSavedBridge && savedBridgeFoundLocally.isPresent()) {
            HueBridge connectedBridge = savedBridgeFoundLocally.get();

            connectedBridge.setUsername(BRIDGE_DATA.getProperty(BridgeProperties.DEVICE_INDENTIFIER));
            connectedBridge.setToken(BRIDGE_DATA.getProperty(BridgeProperties.USERNAME));

            // debug stuff
//            connectedBridge.setActiveLight("00:17:88:01:04:06:45:68-0b", true);


            ACTIVE_BRIDGE = connectedBridge;
        } else {
            // todo: maybe change to more appropriate messages?
            String toastTitle = validSavedBridge ? "Could not find Bridge." : "Set up the Hue Bridge.";
            // todo: display toast at correct time
//            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE, new TextComponent(toastTitle), new TextComponent("Go to the McHue settings.")));
        }
    }
}
