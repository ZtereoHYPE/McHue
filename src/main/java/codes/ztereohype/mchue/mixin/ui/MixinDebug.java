package codes.ztereohype.mchue.mixin.ui;

import codes.ztereohype.mchue.util.DrawingUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenOverlay.class)
public class MixinDebug {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"))
    private void injectRender(PoseStack poseStack, CallbackInfo ci) {
        DrawingUtil.renderLightmapDebug(poseStack);
    }
}
