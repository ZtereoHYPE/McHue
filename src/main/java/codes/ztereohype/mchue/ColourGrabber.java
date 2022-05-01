package codes.ztereohype.mchue;

import codes.ztereohype.mchue.devices.responses.LightState;
import codes.ztereohype.mchue.mixin.LightTextureAccessor;
import codes.ztereohype.mchue.util.MyColourUtil;
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

    public int getGrassBiomeBlend() {
        return minecraft.level.getBlockTint(getPlayerLocation(), BiomeColors.GRASS_COLOR_RESOLVER);
    }

    public int getWaterBiomeBlend() {
        return minecraft.level.getBlockTint(getPlayerLocation(), BiomeColors.WATER_COLOR_RESOLVER);
    }

    public int getLightMultiplier() {
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

        return (r << 16) | (g << 8) | b;
    }

    public int getOverworldColour() {
        if (minecraft.player.isUnderWater()) {
            return getWaterBiomeBlend();
        } else {
            return getGrassBiomeBlend();
        }
    }

    public int getNetherColour() {
        return minecraft.level.getBiomeManager().getBiome(getPlayerLocation()).value().getFogColor();
    }

    public LightState getColour() {
        assert minecraft.player != null;
        LightState colour;

        if (minecraft.player.level.dimension() == Level.NETHER) {
            colour = MyColourUtil.getLightState(MyColourUtil.blendLightColour(getNetherColour(), getLightMultiplier(), 0.3F, 1.4F));
        } else {
            colour = MyColourUtil.getLightState(MyColourUtil.blendLightColour(getOverworldColour(), getLightMultiplier(), 1F, 0.9F));
        }

        return colour;
    }
}
