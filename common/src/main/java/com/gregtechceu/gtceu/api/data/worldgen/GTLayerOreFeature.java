package com.gregtechceu.gtceu.api.data.worldgen;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/3/21
 * @implNote GTLayerOreFeature
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTLayerOreFeature extends Feature<GTLayerOreFeatureConfiguration> {
    public GTLayerOreFeature() {
        super(GTLayerOreFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<GTLayerOreFeatureConfiguration> pContext) {
        RandomSource random = pContext.random();
        BlockPos blockpos = pContext.origin();
        WorldGenLevel worldgenlevel = pContext.level();
        GTLayerOreFeatureConfiguration config = pContext.config();
        List<GTLayerPattern> patternPool = config.getLayerPatterns();

        if (patternPool.isEmpty())
            return false;

        GTLayerPattern GTLayerPattern = patternPool.get(random.nextInt(patternPool.size()));

        int placedAmount = 0;
        int size = config.getEntry().clusterSize;;
        int radius = Mth.ceil(config.getEntry().clusterSize / 2f);
        int x0 = blockpos.getX() - radius;
        int y0 = blockpos.getY() - radius;
        int z0 = blockpos.getZ() - radius;
        int width = size + 1;
        int length = size + 1;
        int height = size + 1;

        if (blockpos.getY() >= worldgenlevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockpos.getX(),
                blockpos.getZ()))
            return false;

        List<GTLayerPattern.Layer> resolvedLayers = new ArrayList<>();
        List<Float> layerDiameterOffsets = new ArrayList<>();

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        BulkSectionAccess bulksectionaccess = new BulkSectionAccess(worldgenlevel);
        int layerCoordinate = random.nextInt(4);
        int slantyCoordinate = random.nextInt(3);
        float slope = random.nextFloat() * .75f;

        try {

            for (int x = 0; x < width; x++) {
                float dx = x * 2f / width - 1;
                if (dx * dx > 1)
                    continue;

                for (int y = 0; y < height; y++) {
                    float dy = y * 2f / height - 1;
                    if (dx * dx + dy * dy > 1)
                        continue;
                    if (worldgenlevel.isOutsideBuildHeight(y0 + y))
                        continue;

                    for (int z = 0; z < length; z++) {
                        float dz = z * 2f / height - 1;

                        int layerIndex = layerCoordinate == 0 ? z : layerCoordinate == 1 ? x : y;
                        if (slantyCoordinate != layerCoordinate)
                            layerIndex += Mth.floor(slantyCoordinate == 0 ? z : slantyCoordinate == 1 ? x : y) * slope;

                        while (layerIndex >= resolvedLayers.size()) {
                            GTLayerPattern.Layer next = GTLayerPattern.rollNext(
                                    resolvedLayers.isEmpty() ? null : resolvedLayers.get(resolvedLayers.size() - 1),
                                    random);
                            float offset = random.nextFloat() * .5f + .5f;
                            for (int i = 0; i < next.minSize + random.nextInt(1 + next.maxSize - next.minSize); i++) {
                                resolvedLayers.add(next);
                                layerDiameterOffsets.add(offset);
                            }
                        }

                        if (dx * dx + dy * dy + dz * dz > 1 * layerDiameterOffsets.get(layerIndex))
                            continue;

                        GTLayerPattern.Layer layer = resolvedLayers.get(layerIndex);
                        List<OreConfiguration.TargetBlockState> state = layer.rollBlock(random);

                        int currentX = x0 + x;
                        int currentY = y0 + y;
                        int currentZ = z0 + z;

                        mutablePos.set(currentX, currentY, currentZ);
                        if (!worldgenlevel.ensureCanWrite(mutablePos))
                            continue;
                        LevelChunkSection levelchunksection = bulksectionaccess.getSection(mutablePos);
                        if (levelchunksection == null)
                            continue;

                        int i3 = SectionPos.sectionRelative(currentX);
                        int j3 = SectionPos.sectionRelative(currentY);
                        int k3 = SectionPos.sectionRelative(currentZ);
                        BlockState blockstate = levelchunksection.getBlockState(i3, j3, k3);

                        for (OreConfiguration.TargetBlockState oreconfiguration$targetblockstate : state) {
                            if (!canPlaceOre(blockstate, bulksectionaccess::getBlockState, random, config, oreconfiguration$targetblockstate, mutablePos))
                                continue;
                            if (oreconfiguration$targetblockstate.state.isAir())
                                continue;
                            levelchunksection.setBlockState(i3, j3, k3, oreconfiguration$targetblockstate.state, false);
                            ++placedAmount;
                            break;
                        }

                    }
                }
            }

        } catch (Throwable throwable1) {
            try {
                bulksectionaccess.close();
            } catch (Throwable throwable) {
                throwable1.addSuppressed(throwable);
            }

            throw throwable1;
        }

        bulksectionaccess.close();
        return placedAmount > 0;
    }

    public boolean canPlaceOre(BlockState pState, Function<BlockPos, BlockState> pAdjacentStateAccessor,
                               RandomSource pRandom, GTLayerOreFeatureConfiguration pConfig, OreConfiguration.TargetBlockState pTargetState,
                               BlockPos.MutableBlockPos pMatablePos) {
        if (!pTargetState.target.test(pState, pRandom))
            return false;
        if (shouldSkipAirCheck(pRandom, pConfig.discardChanceOnAirExposure))
            return true;

        return !isAdjacentToAir(pAdjacentStateAccessor, pMatablePos);
    }

    protected boolean shouldSkipAirCheck(RandomSource pRandom, float pChance) {
        return pChance <= 0 || (!(pChance >= 1) && pRandom.nextFloat() >= pChance);
    }
}

