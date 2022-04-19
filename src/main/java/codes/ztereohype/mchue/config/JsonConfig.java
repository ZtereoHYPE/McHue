package codes.ztereohype.mchue.config;

import codes.ztereohype.mchue.McHue;
import net.shadew.json.*;
import org.apache.logging.log4j.Level;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    },
    connectedLights: [
        {
            uniqueId: "00:17:88:01:00:17:7a:b0-0b",
            name: "Living Colors",
            index: 4,
        }
    ]
}
*/

public class JsonConfig {
    private final String COMMENT;
    private final Json JSON = Json.json5Builder().formatConfig(FormattingConfig.pretty().useIdentifierKeys(true))
                                  .build();
    private final Path path;
    private JsonNode root;
    private JsonNode defaultRoot;

    public JsonConfig(Path path, String comment, String defaultRoot) {
        this.path = path;
        COMMENT = comment;
        try {
            this.defaultRoot = JSON.parse(defaultRoot);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public <T extends Enum<T> & JsonPropertiesEnum> void setProperty(T propertiesEnum, String value) {
        if (!root.query(propertiesEnum.getPath()).is(JsonType.STRING) && !root.query(propertiesEnum.getPath())
                                                                              .is(JsonType.NUMBER)) {
            //todo: throw error?
            return;
        }

        //todo: find a way to set from the path to the object the object's value

        save();
    }

    public <T extends Enum<T> & JsonPropertiesEnum> String getProperty(T propertiesEnum) {
        return root.query(propertiesEnum.getPath()).asString();
    }

    private void init() throws IOException {
        load();

        for (String key : defaultRoot.keySet()) {
            if (root.has(key)) continue;
            root.append(defaultRoot.get(key));
        }

        save();
    }

    private void load() throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        this.root = JSON.parse(Files.newInputStream(path));
    }

    private void save() {
        File file = new File(String.valueOf(path));
        String output = "//" + COMMENT + System.lineSeparator() + JSON.serialize(root);

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(output);
            out.close();
            McHue.LOGGER.log(Level.INFO, "Created config.");
        } catch (IOException e) {
            e.printStackTrace();
            McHue.LOGGER.log(Level.ERROR, "There was an error trying to save the config. Please contact the developer and send them the log.");
        }
    }
}
