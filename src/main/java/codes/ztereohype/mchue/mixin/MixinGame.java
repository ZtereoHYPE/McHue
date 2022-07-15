package codes.ztereohype.mchue.mixin;

import codes.ztereohype.mchue.LightColourScheduler;
import net.minecraft.client.Game;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Game.class)
public class MixinGame {
    @Inject(at = @At("HEAD"), method = "onStartGameSession()V")
    public void injectUpdaterStart(CallbackInfo ci) {
        LightColourScheduler.startUpdater();
    }

    @Inject(at = @At("HEAD"), method = "onLeaveGameSession()V")
    public void injectUpdaterStop(CallbackInfo ci) {
        LightColourScheduler.stopUpdater();
    }
}
