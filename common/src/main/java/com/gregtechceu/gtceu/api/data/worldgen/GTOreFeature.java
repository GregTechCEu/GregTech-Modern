package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
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
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTOreFeature
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTOreFeature extends Feature<GTOreFeatureConfiguration> {

    public GTOreFeature() {
        super(GTOreFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<GTOreFeatureConfiguration> context) {
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        GTOreFeatureEntry entry = context.config().getEntry(random);
        context.config().setEntry(null);
        MutableBoolean returnValue = new MutableBoolean(false);

        GTCEu.LOGGER.info("trying to place vein " + entry.id + " at " + origin);
        entry.targets.ifLeft(targets -> {
            float f = random.nextFloat() * (float)Math.PI;
            float f1 = (float)entry.clusterSize / 8.0F;
            int i = Mth.ceil(((float)entry.clusterSize / 16.0F * 2.0F + 1.0F) / 2.0F);
            double d0 = origin.getX() + Math.sin(f) * f1;
            double d1 = origin.getX() - Math.sin(f) * f1;
            double d2 = origin.getZ() + Math.cos(f) * f1;
            double d3 = origin.getZ() - Math.cos(f) * f1;
            double d4 = origin.getY() + random.nextInt(3) - 2;
            double d5 = origin.getY() + random.nextInt(3) - 2;
            int k = origin.getX() - Mth.ceil(f1) - i;
            int l = origin.getY() - 2 - i;
            int i1 = origin.getZ() - Mth.ceil(f1) - i;
            int j1 = 2 * (Mth.ceil(f1) + i);
            int k1 = 2 * (2 + i);

            for(int l1 = k; l1 <= k + j1; ++l1) {
                for(int i2 = i1; i2 <= i1 + j1; ++i2) {
                    if (l <= level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, l1, i2)) {
                        if (this.doPlaceNormal(level, random, entry, targets, d0, d1, d2, d3, d4, d5, k, l, i1, j1, k1)) {
                            returnValue.setTrue();
                            return;
                        }
                    }
                }
            }
        }).ifRight(layers -> {
            if (this.doPlaceLayer(level, random, entry, origin, layers)) {
                returnValue.setTrue();
            }
        });

        GTCEu.LOGGER.info("did place feature: " + returnValue.isTrue());
        return returnValue.getValue();
    }

    protected boolean doPlaceNormal(WorldGenLevel pLevel, RandomSource pRandom, GTOreFeatureEntry entry, List<OreConfiguration.TargetBlockState> targets,
                                    double pMinX, double pMaxX, double pMinZ, double pMaxZ, double pMinY, double pMaxY, int pX, int pY, int pZ,
                                    int pWidth, int pHeight) {
        int i = 0;
        BitSet bitset = new BitSet(pWidth * pHeight * pWidth);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int j = entry.clusterSize;
        double[] adouble = new double[j * 4];

        for(int k = 0; k < j; ++k) {
            float f = (float)k / (float)j;
            double d0 = Mth.lerp(f, pMinX, pMaxX);
            double d1 = Mth.lerp(f, pMinY, pMaxY);
            double d2 = Mth.lerp(f, pMinZ, pMaxZ);
            double d3 = pRandom.nextDouble() * (double)j / 16.0D;
            double d4 = ((double)(Mth.sin((float)Math.PI * f) + 1.0F) * d3 + 1.0D) / 2.0D;
            adouble[k * 4] = d0;
            adouble[k * 4 + 1] = d1;
            adouble[k * 4 + 2] = d2;
            adouble[k * 4 + 3] = d4;
        }

        for(int l3 = 0; l3 < j - 1; ++l3) {
            if (!(adouble[l3 * 4 + 3] <= 0.0D)) {
                for(int i4 = l3 + 1; i4 < j; ++i4) {
                    if (!(adouble[i4 * 4 + 3] <= 0.0D)) {
                        double d8 = adouble[l3 * 4] - adouble[i4 * 4];
                        double d10 = adouble[l3 * 4 + 1] - adouble[i4 * 4 + 1];
                        double d12 = adouble[l3 * 4 + 2] - adouble[i4 * 4 + 2];
                        double d14 = adouble[l3 * 4 + 3] - adouble[i4 * 4 + 3];
                        if (d14 * d14 > d8 * d8 + d10 * d10 + d12 * d12) {
                            if (d14 > 0.0D) {
                                adouble[i4 * 4 + 3] = -1.0D;
                            } else {
                                adouble[l3 * 4 + 3] = -1.0D;
                            }
                        }
                    }
                }
            }
        }

        BulkSectionAccess bulksectionaccess = new BulkSectionAccess(pLevel);

        try {
            for(int j4 = 0; j4 < j; ++j4) {
                double d9 = adouble[j4 * 4 + 3];
                if (!(d9 < 0.0D)) {
                    double d11 = adouble[j4 * 4];
                    double d13 = adouble[j4 * 4 + 1];
                    double d15 = adouble[j4 * 4 + 2];
                    int k4 = Math.max(Mth.floor(d11 - d9), pX);
                    int l = Math.max(Mth.floor(d13 - d9), pY);
                    int i1 = Math.max(Mth.floor(d15 - d9), pZ);
                    int j1 = Math.max(Mth.floor(d11 + d9), k4);
                    int k1 = Math.max(Mth.floor(d13 + d9), l);
                    int l1 = Math.max(Mth.floor(d15 + d9), i1);

                    for(int i2 = k4; i2 <= j1; ++i2) {
                        double d5 = ((double)i2 + 0.5D - d11) / d9;
                        if (d5 * d5 < 1.0D) {
                            for(int j2 = l; j2 <= k1; ++j2) {
                                double d6 = ((double)j2 + 0.5D - d13) / d9;
                                if (d5 * d5 + d6 * d6 < 1.0D) {
                                    for(int k2 = i1; k2 <= l1; ++k2) {
                                        double d7 = ((double)k2 + 0.5D - d15) / d9;
                                        if (d5 * d5 + d6 * d6 + d7 * d7 < 1.0D && !pLevel.isOutsideBuildHeight(j2)) {
                                            int l2 = i2 - pX + (j2 - pY) * pWidth + (k2 - pZ) * pWidth * pHeight;
                                            if (!bitset.get(l2)) {
                                                bitset.set(l2);
                                                blockpos$mutableblockpos.set(i2, j2, k2);
                                                if (pLevel.ensureCanWrite(blockpos$mutableblockpos)) {
                                                    LevelChunkSection levelchunksection = bulksectionaccess.getSection(blockpos$mutableblockpos);
                                                    if (levelchunksection != null) {
                                                        int i3 = SectionPos.sectionRelative(i2);
                                                        int j3 = SectionPos.sectionRelative(j2);
                                                        int k3 = SectionPos.sectionRelative(k2);
                                                        BlockState blockstate = levelchunksection.getBlockState(i3, j3, k3);

                                                        for(OreConfiguration.TargetBlockState oreconfiguration$targetblockstate : targets) {
                                                            if (canPlaceOre(blockstate, bulksectionaccess::getBlockState, pRandom, entry, oreconfiguration$targetblockstate, blockpos$mutableblockpos)) {
                                                                levelchunksection.setBlockState(i3, j3, k3, oreconfiguration$targetblockstate.state, false);
                                                                ++i;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
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
        return i > 0;
    }

    protected boolean doPlaceLayer(WorldGenLevel level, RandomSource random, GTOreFeatureEntry entry, BlockPos origin, List<GTLayerPattern> patternPool) {
        if (patternPool.isEmpty())
            return false;

        GTLayerPattern layerPattern = patternPool.get(random.nextInt(patternPool.size()));

        int placedAmount = 0;
        int size = entry.clusterSize;
        int radius = Mth.ceil(entry.clusterSize / 2f);
        int x0 = origin.getX() - radius;
        int y0 = origin.getY() - radius;
        int z0 = origin.getZ() - radius;
        int width = size + 1;
        int length = size + 1;
        int height = size + 1;

        if (origin.getY() >= level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(),
                origin.getZ()))
            return false;

        List<GTLayerPattern.Layer> resolvedLayers = new ArrayList<>();
        List<Float> layerDiameterOffsets = new ArrayList<>();

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        BulkSectionAccess bulksectionaccess = new BulkSectionAccess(level);
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
                    if (level.isOutsideBuildHeight(y0 + y))
                        continue;

                    for (int z = 0; z < length; z++) {
                        float dz = z * 2f / height - 1;

                        int layerIndex = layerCoordinate == 0 ? z : layerCoordinate == 1 ? x : y;
                        if (slantyCoordinate != layerCoordinate)
                            layerIndex += Mth.floor(slantyCoordinate == 0 ? z : slantyCoordinate == 1 ? x : y) * slope;

                        while (layerIndex >= resolvedLayers.size()) {
                            GTLayerPattern.Layer next = layerPattern.rollNext(
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
                        if (!level.ensureCanWrite(mutablePos))
                            continue;
                        LevelChunkSection levelchunksection = bulksectionaccess.getSection(mutablePos);
                        if (levelchunksection == null)
                            continue;

                        int i3 = SectionPos.sectionRelative(currentX);
                        int j3 = SectionPos.sectionRelative(currentY);
                        int k3 = SectionPos.sectionRelative(currentZ);
                        BlockState blockstate = levelchunksection.getBlockState(i3, j3, k3);

                        for (OreConfiguration.TargetBlockState oreconfiguration$targetblockstate : state) {
                            if (!canPlaceOre(blockstate, bulksectionaccess::getBlockState, random, entry, oreconfiguration$targetblockstate, mutablePos))
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
                               RandomSource pRandom, GTOreFeatureEntry entry, OreConfiguration.TargetBlockState pTargetState,
                               BlockPos.MutableBlockPos pMatablePos) {
        if (!pTargetState.target.test(pState, pRandom))
            return false;
        if (shouldSkipAirCheck(pRandom, entry.discardChanceOnAirExposure))
            return true;

        return !isAdjacentToAir(pAdjacentStateAccessor, pMatablePos);
    }

    protected boolean shouldSkipAirCheck(RandomSource pRandom, float pChance) {
        return pChance <= 0 || (!(pChance >= 1) && pRandom.nextFloat() >= pChance);
    }

}
