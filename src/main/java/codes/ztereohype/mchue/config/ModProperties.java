package codes.ztereohype.mchue.config;

import lombok.Getter;

import java.util.Locale;

public enum ModProperties implements PropertiesEnum {
    IS_ACTIVE("true", false),
    ENTERTAINMENT_ZONES("false", false);


    final @Getter String defaultValue;
    final @Getter boolean isArray;

    ModProperties(String defaultValue, boolean isArray) {
        this.defaultValue = defaultValue;
        this.isArray = isArray;
    }

    @Override
    public String getSettingName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}