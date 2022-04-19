package codes.ztereohype.mchue.config;

import lombok.Getter;

import java.util.Locale;

public enum BridgeProperties implements PropertiesEnum {
    BRIDGE_ID("null", false),
    BRIDGE_IP("null", false),
    DEVICE_INDENTIFIER("null", false),
    USERNAME("null", false),
    CONNECTED_LIGHTS("null", true);

    final @Getter String defaultValue;
    final @Getter boolean isArray;

    BridgeProperties(String defaultValue, boolean isArray) {
        this.defaultValue = defaultValue;
        this.isArray = isArray;
    }

    @Override
    public String getSettingName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
