package codes.ztereohype.mchue.config;

import codes.ztereohype.mchue.McHue;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/*
Config rewrite plan:
- Json5 with comments eplaining parts if possible
- getProperty will have 2 overloads:
    - Json mode: accepts a Json Query object (from root!) and returns an (optional?) JsonNode that will have to be interpreted by the other side
    - BridgeProperties enum mode: If an enum is used the enum content will be read (it will contain a specific query)
- setProperty, similarly, will have 2 overloads and *will save at every set-ing*:
    - Json mode: accepts a JsonNode (from root!) and a value and will .append it to the root of the parsed json config
    - BridgeProperties enum mode: The enum content will be treates as the query to be done on the bridge of the mainID.
- deleteProperty will accept a JsonNode and attempt to remove the entire thing from the base.
- load will cache the whole parsed json in a JsonNode property in the config.
- save will save the current jsonnode property to the file, writing it in a beautiful way.


Structure:
{
    connectedBridge: {
        id: "bridgeid1",
        ip: "127.0.0.1",
        deviceId: "mchue#unknown",
        username: null             ---> this CANNOT HAPPEN; the bridgeid1 object will be deleted if this is the selected bridge ID and the user will be prompted with selecting a bridge from the list of valid ones.
    }, // known bridges too perhaps?
    connectedLights: [
        {
            uniqueId: "00:17:88:01:00:17:7a:b0-0b",
            name: "Living Colors",
            id: 4,
        }
    ]
}
*/

//todo: move away from this shithole of a system to the cleaner json one pls
public class Config {
    private final String HEADER_COMMENT;
    private final Properties properties = new Properties();
    private final Map<String, String> defaultProperties;

    Path configPath;

    public Config(Path configPath, String headerComment, Map<String, String> defaultProperties) {
        this.HEADER_COMMENT = headerComment;
        this.configPath = configPath;
        this.defaultProperties = defaultProperties;
    }

    public <T extends Enum<T> & PropertiesEnum> void setProperty(T propertiesEnum, String value) {
        if (propertiesEnum.isArray()) return;
        properties.setProperty(propertiesEnum.getSettingName(), value);
        save();
    }

    public <T extends Enum<T> & PropertiesEnum> void setPropertyArray(T propertiesEnum, String[] values) {
        if (!propertiesEnum.isArray()) return;

        StringBuilder stringifiedValue = new StringBuilder();

        for (String eachstring : values) {
            stringifiedValue.append(eachstring).append(",");
        }

        properties.setProperty(propertiesEnum.getSettingName(), stringifiedValue.toString());

        if (stringifiedValue.isEmpty())
            properties.setProperty(propertiesEnum.getSettingName(), propertiesEnum.getDefaultValue());

        save();
    }

    public <T extends Enum<T> & PropertiesEnum> String getProperty(T propertiesEnum) {
        // use the getPropertyArray pls
        if (propertiesEnum.isArray()) return "null";

        String key = propertiesEnum.getSettingName();
        return properties.containsKey(key) ? properties.getProperty(key) : defaultProperties.get(key);
    }

    public <T extends Enum<T> & PropertiesEnum> String[] getPropertyArray(T propertiesEnum) {
        // use the getProperty pls
        if (!propertiesEnum.isArray()) return new String[]{};

        String key = propertiesEnum.getSettingName();
        String mergedProps = properties.containsKey(key) ? properties.getProperty(key) : defaultProperties.get(key);

        return mergedProps.split(",", 0);
    }

    public void initialise() throws IOException {
        load();

        AtomicBoolean changed = new AtomicBoolean(false);
        defaultProperties.forEach((key, value) -> {
            if (!properties.containsKey(key)) {
                properties.setProperty(key, value);
                changed.set(true);
            }
        });

        if (changed.get()) save();
    }

    private void load() throws IOException {
        if (!Files.exists(configPath)) {
            return;
        }

        properties.load(Files.newInputStream(configPath));
    }

    private void save() {
        // work around Properties::store not being able to create directories.
        File configDir = new File(FilenameUtils.getPath(configPath.toString()));
        boolean createdDirs = configDir.mkdirs();
        if (createdDirs) McHue.LOGGER.log(Level.INFO, "Created config directories.");

        try {
            properties.store(Files.newOutputStream(configPath), HEADER_COMMENT);
        } catch (IOException e) {
            e.printStackTrace();
            McHue.LOGGER.error("There was an error trying to save the config. Please contact the developer and send them the log.");
        }
    }
}
