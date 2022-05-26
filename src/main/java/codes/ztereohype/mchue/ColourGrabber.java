package codes.ztereohype.mchue;

import codes.ztereohype.mchue.devices.interfaces.LightState;
import codes.ztereohype.mchue.mixin.LightTextureAccessor;
import codes.ztereohype.mchue.util.ColourUtil;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ColourGrabber {
    private final Minecraft minecraft = Minecraft.getInstance();
    private final Level level = Minecraft.getInstance().level;

    private final TagKey<Block> CAVE_TAG = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("mchue", "cave_blocks"));
    private final Vec3[] caveCheckVectors;

//    private BlockPos playerPos;
//
//    private int grassColour;
//    private int waterColour;
//    private int skyColour;
//    private int lightColour;

    public ColourGrabber() {
        this.caveCheckVectors = getFibSphereVectors(30);
    }

    private Vec3[] getFibSphereVectors(int samples) {
        List<Vec3> vectors = new ArrayList<>();
        double phi = Math.PI * (3 - Math.sqrt(5));

        for (int i = samples/4; i < samples; i++) {
            double y = 1 - (i / (float) (samples - 1)) * 2; // y from 1 to -1

            double radius = Math.sqrt(1 - y * y);
            double theta = phi * i;

            double x = Math.cos(theta) * radius;
            double z = Math.sin(theta) * radius;

            vectors.add(new Vec3(x, -y, z));
        }
        return vectors.toArray(Vec3[]::new);
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
        float caveness = checkCave();
        if (minecraft.player.isUnderWater()) {
            return getWaterBiomeBlend();
        } else if (caveness > 0){
            return ColourUtil.lerpColours(getGrassBiomeBlend(), getCaveColour(), caveness);
        } else {
            return getGrassBiomeBlend();
        }
    }

    private LightState getCaveColour() {
        return new LightState(0x99, 0x99, 0x99);
    }

    public LightState getNetherColour() {
        return new LightState(level.getBiomeManager().getBiome(getPlayerLocation()).value().getFogColor());
    }

//    public LightState getSkyColour() {
////        return new LightState(minecraft.level.getSkyColor(new Vec3(getPlayerLocation().), 0F));
//    }

    private float checkCave() {
        Vec3 startPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        int hits = 0;

        for (Vec3 caveCheckVector : caveCheckVectors) {
            Vec3 end = startPos.add(caveCheckVector.scale(100)); // todo unhardcode (later too)

            BlockHitResult hitResult = level.clip(new ClipContext(startPos, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, minecraft.player));

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                int skyLight = level.getBrightness(LightLayer.SKY, hitResult.getBlockPos());

                // look for the block that potentially has access to skylight around the hit one if we have 0 skylight (eg air next to cave wall)
                if (skyLight == 0) {
                    for (Direction direction : Direction.values()) {
                        skyLight = Math.max(skyLight, level.getBrightness(LightLayer.SKY, hitResult.getBlockPos()
                                                                                                   .relative(direction)));
                    }
                }

                BlockState hitBlock = level.getBlockState(hitResult.getBlockPos());

                if (hitBlock.is(CAVE_TAG) && skyLight < 14) {//todo unhardcode these values
                    hits++;
                }
            }
        }

        if (hits < caveCheckVectors.length / 2F) {
            return 0;
        } else {
            return ((hits / (float) caveCheckVectors.length) - 0.5F) * 2;
        }
    }

    public LightState getColour() {
        System.out.println(checkCave());
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
