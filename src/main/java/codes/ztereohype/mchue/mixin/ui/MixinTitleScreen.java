package codes.ztereohype.mchue.mixin.ui;

import codes.ztereohype.mchue.McHue;
import codes.ztereohype.mchue.gui.screens.LightConfigurationScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {
    protected MixinTitleScreen(Component component) {
        super(component);
    }

    @Inject(at = @At("HEAD"), method = "createNormalMenuOptions(II)V")
    private void createNormalMenuOptions(int y, int spacingY, CallbackInfo callbackInfo) {
        codes.ztereohype.mchue.McHue.settingsScreen = new LightConfigurationScreen(this);
        this.addRenderableWidget(new Button(50, 50, 150, 20, Component.nullToEmpty("McHue Settings"), button -> this.minecraft.setScreen(codes.ztereohype.mchue.McHue.settingsScreen)));
    }

//    @Inject(at = @At("HEAD"), method = "init()V")
//    private void injectInit(CallbackInfo ci) {
//        System.out.println("called createNormalMenuOptions");
//        McHue.getInitialisedInstance().displayToastIfYouShould();
//    }
}