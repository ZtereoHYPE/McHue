package codes.ztereohype.mchue;

import codes.ztereohype.mchue.devices.interfaces.LightState;
import codes.ztereohype.mchue.mixin.LightTextureAccessor;
import codes.ztereohype.mchue.util.ColourUtil;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ColourGrabber {
    private final Minecraft minecraft = Minecraft.getInstance();
    private final Level level = Minecraft.getInstance().level;

    private final Map<Block, Integer> blockColours = new HashMap<>();
    private final Vec3[] floorVectors;

    private final TagKey<Block> CAVE_TAG = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("mchue", "cave_blocks"));
    private final Vec3[] caveCheckVectors;


    //trash
    private final List<Integer> skyLights = new IntArrayList();
    private final List<Integer> blockLights = new IntArrayList();

    public ColourGrabber() {
        this.caveCheckVectors = getFibSphereVectors(75, false);
        this.floorVectors = getFibSphereVectors(50, true);
        calculateBlockColours();
    }

    private void calculateBlockColours() {
        Registry.BLOCK.stream().forEach(block -> blockColours.put(block, block.defaultBlockState().getMapColor(level, new BlockPos(0, 0, 0)).col));
    }

    private Vec3[] getFibSphereVectors(int samples, boolean floor) {
        List<Vec3> vectors = new ArrayList<>();
        double phi = Math.PI * (3 - Math.sqrt(5));

        //todo: fix this to accept 4 parameters (floor walls cieling full)
        for (int i = samples / 4; i < samples; i++) {
            double y;

            if (floor) {
                y = (i / (float) (samples - 1)); // y from -1 to 0
            } else {
                y = 1 - (i / (float) (samples - 1)) * 2; // y from 1 to -1
            }

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

    /**
     * Gets the colour of the block at the given position
     *
     * @param type the {@link ColorResolver} to use, obtained from {@link BiomeColors}
     * @param pos  the BlockPos to get the colour of the biome blend from
     * @return the colour of the block at the given position
     */
    public LightState getBiomeBlendColour(ColorResolver type, BlockPos pos) {
        return new LightState(minecraft.level.getBlockTint(pos, type));
    }

    public LightState getLightMultiplier() {
        int skyLight = 0;
        int blockLight = 0;
        for (int light : skyLights) {
            skyLight += light;
        }
        for (int light : blockLights) {
            blockLight += light;
        }

        skyLight /= Math.max(1, skyLights.size());
        blockLight /= Math.max(1, blockLights.size());

        NativeImage lightPixels = ((LightTextureAccessor) minecraft.gameRenderer.lightTexture()).getLightPixels();

        // abgr -> rgb
        int pixelRGBA = lightPixels.getPixelRGBA(skyLight, blockLight);
        int bgr = pixelRGBA & 0x00FFFFFF;
        int b = (bgr >> 16) & 0xFF;
        int g = (bgr >> 8) & 0xFF;
        int r = bgr & 0xFF;

        return new LightState(r, g, b);
    }

    private LightState getBlockColour(@NonNull Block block, BlockPos pos) {
        if (GrassBlock.class.equals(block.getClass())) {
            return getBiomeBlendColour(BiomeColors.GRASS_COLOR_RESOLVER, pos);
        } else if (block instanceof LiquidBlock && block.defaultBlockState().getFluidState().is(FluidTags.WATER)) {
            return getBiomeBlendColour(BiomeColors.WATER_COLOR_RESOLVER, pos);

        // todo: use leaves colour BUT not for leaves that don't use the leaves colour blend (??)
        } else if (block.defaultBlockState().is(BlockTags.LEAVES)) {
            return getBiomeBlendColour(BiomeColors.FOLIAGE_COLOR_RESOLVER, pos);
        } else {
            return new LightState(blockColours.get(block));
        }
    }

    private LightState getCaveColour(float caveness) {
        //todo redo this
        return new LightState(0x99, 0x99, 0x99);
    }

    public LightState getNetherColour() {
        return new LightState(level.getBiomeManager().getBiome(getPlayerLocation()).value().getFogColor());
    }

    public LightState getSkyColour() {
        BlockPos pos = getPlayerLocation();
        Vec3 colour = minecraft.level.getSkyColor(new Vec3(pos.getX(), pos.getY(), pos.getZ()), 0F);
        return new LightState((int) colour.x * 255, (int) colour.y * 255, (int) colour.z * 255);
    }

    private float checkCave() {
        blockLights.clear();
        skyLights.clear();

        int hits = 0;

        for (Vec3 caveCheckVector : caveCheckVectors) {
            BlockHitResult hitResult = castRay(caveCheckVector, 100, true);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                int skyLight = level.getBrightness(LightLayer.SKY, hitResult.getBlockPos());
                int blockLight = level.getBrightness(LightLayer.BLOCK, hitResult.getBlockPos());

                // look for the block that potentially has access to skylight around the hit one if we have 0 skylight (eg air next to cave wall)
                //todo: this is flawed as it will pick the bigger one always, do a +1 check to avoid most cases
                if (skyLight == 0) {
                    for (Direction direction : Direction.values()) {
                        skyLight = Math.max(skyLight, level.getBrightness(LightLayer.SKY, hitResult.getBlockPos().relative(direction)));
                    }
                }
                if (blockLight == 0) {
                    for (Direction direction : Direction.values()) {
                        blockLight = Math.max(blockLight, level.getBrightness(LightLayer.BLOCK, hitResult.getBlockPos().relative(direction)));
                    }
                }

                skyLights.add(skyLight);
                blockLights.add(blockLight);

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

    private LightState getFloorPrevalentColour() {
        List<Block> hitList = new ArrayList<>();

        for (Vec3 floorVec : floorVectors) {
            BlockHitResult hitResult = castRay(floorVec, 20, false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                hitList.add(level.getBlockState(hitResult.getBlockPos()).getBlock());
            }
        }

        List<Map.Entry<Block, Long>> mostCommonBlocks = new ArrayList<>(hitList.stream().parallel()
                                                                               .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                                                                               .entrySet()
                                                                               .stream()
                                                                               .toList());
        // sort by count reversed
        mostCommonBlocks.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        float caveness = checkCave();
        if (mostCommonBlocks.isEmpty()) return caveness > 0 ? getCaveColour(caveness) : getSkyColour();

        BlockPos playerLocation = getPlayerLocation();

        // only use the one or two most common blocks
        if (mostCommonBlocks.size() < 2) {
            return getBlockColour(mostCommonBlocks.get(0).getKey(), playerLocation);
        } else {
            LightState colour1 = getBlockColour(mostCommonBlocks.get(0).getKey(), playerLocation);
            LightState colour2 = getBlockColour(mostCommonBlocks.get(1).getKey(), playerLocation);

            float ratio = mostCommonBlocks.get(1).getValue() / (float) (mostCommonBlocks.get(0)
                                                                                        .getValue() + mostCommonBlocks.get(1)
                                                                                                                      .getValue());

            return ColourUtil.lerpColours(colour1, colour2, ratio);
        }
    }

    private BlockHitResult castRay(Vec3 direction, int distance, boolean ignoreFluid) {
        Vec3 startPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 end = startPos.add(direction.scale(distance));

        ClipContext.Fluid fluidClipContext = ignoreFluid ? ClipContext.Fluid.NONE : ClipContext.Fluid.SOURCE_ONLY;

        return level.clip(new ClipContext(startPos, end, ClipContext.Block.COLLIDER, fluidClipContext, minecraft.player));
    }

    public LightState getColour() {
        LightState colour;
        if (minecraft.player.level.dimension() == Level.NETHER) {
            colour = ColourUtil.blendLightColour(getNetherColour(), getLightMultiplier(), 0.3F, 1.4F);
        } else {
            colour = ColourUtil.blendLightColour(getFloorPrevalentColour(), getLightMultiplier(), 0.7F, 0.9F);
        }

        return colour;
    }
}
