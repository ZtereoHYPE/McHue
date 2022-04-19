package codes.ztereohype.mchue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public class ColourGrabber {
    //use net.minecraft.world.level.BlockAndTintGetter
    private final Minecraft minecraft;

    public ColourGrabber(Minecraft minecraft) {
        this.minecraft = minecraft;
    }
    // TODO: make sure this is only called when there is a player and world n shit
    public int getBiome() {
        return minecraft.level.getBlockTint(new BlockPos(minecraft.player.getEyePosition()), BiomeColors.GRASS_COLOR_RESOLVER);
//        return minecraft.level.getBiomeName(new BlockPos(minecraft.player.getEyePosition())).get().location().toString();
    }
}
