package codes.ztereohype.mchue.compat;

import codes.ztereohype.mchue.gui.screens.LightSelectionScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuSettingsApi implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LightSelectionScreen::new;
    }
}
