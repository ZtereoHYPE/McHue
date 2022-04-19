package codes.ztereohype.mchue.mixin;

import codes.ztereohype.mchue.LightColourScheduler;
import net.minecraft.client.Game;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Game.class)
public class MixinGame {
    @Shadow @Final private Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "onStartGameSession()V")
    public void injectUpdaterStart(CallbackInfo ci) {
        LightColourScheduler.startUpdater(this.minecraft);
    }

    @Inject(at = @At("HEAD"), method = "onLeaveGameSession()V")
    public void injectUpdaterStop(CallbackInfo ci) {
        LightColourScheduler.stopUpdater();
    }
}
