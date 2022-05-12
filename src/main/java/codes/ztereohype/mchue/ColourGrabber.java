package codes.ztereohype.mchue;

import codes.ztereohype.mchue.devices.interfaces.LightState;
import codes.ztereohype.mchue.mixin.LightTextureAccessor;
import codes.ztereohype.mchue.util.ColourUtil;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class ColourGrabber {
    private final Minecraft minecraft;

    public ColourGrabber(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    private BlockPos getPlayerLocation() {
        return new BlockPos(minecraft.player.getEyePosition());
    }

    public LightState getGrassBiomeBlend() {
        return new LightState(minecraft.level.getBlockTint(getPlayerLocation(), BiomeColors.GRASS_COLOR_RESOLVER));
    }

    public LightState getWaterBiomeBlend() {
        return new LightState(minecraft.level.getBlockTint(getPlayerLocation(), BiomeColors.WATER_COLOR_RESOLVER));
    }

    public LightState getLightMultiplier() {
        Level clientLevel = this.minecraft.level;
        BlockPos playerLocation = getPlayerLocation();

        int skyLight = clientLevel.getBrightness(LightLayer.SKY, playerLocation);
        int blockLight = clientLevel.getBrightness(LightLayer.BLOCK, playerLocation);

        NativeImage lightPixels = ((LightTextureAccessor) minecraft.gameRenderer.lightTexture()).getLightPixels();

        // abgr -> rgb
        int bgr = lightPixels.getPixelRGBA(blockLight, skyLight) & 0x00FFFFFF;
        int b = (bgr >> 16) & 0xFF;
        int g = (bgr >> 8) & 0xFF;
        int r = bgr & 0xFF;

        return new LightState(r, g, b);
    }

    public LightState getOverworldColour() {
        if (minecraft.player.isUnderWater()) {
            return getWaterBiomeBlend();
        } else {
            return getGrassBiomeBlend();
        }
    }

    public LightState getNetherColour() {
        return new LightState(minecraft.level.getBiomeManager().getBiome(getPlayerLocation()).value().getFogColor());
    }

    public LightState getColour() {
        assert minecraft.player != null;
        LightState colour;

        if (minecraft.player.level.dimension() == Level.NETHER) {
            colour = ColourUtil.blendLightColour(getNetherColour(), getLightMultiplier(), 0.3F, 1.4F);
        } else {
            colour = ColourUtil.blendLightColour(getOverworldColour(), getLightMultiplier(), 0.7F, 0.9F);
        }

        return colour;
    }
}
