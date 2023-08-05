package com.gregtechceu.gtceu.api.data.worldgen.strata;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.*;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public interface IStrataLayer extends StringRepresentable {
    Codec<IStrataLayer> CODEC = ExtraCodecs.stringResolverCodec(StringRepresentable::getSerializedName, WorldGeneratorUtils.STRATA_LAYERS::get);

    boolean isNatural();

    Supplier<Supplier<BlockState>> getState();

    Material getMaterial();

    TagPrefix getTagPrefix();

    @Nullable
    default VerticalAnchor getHeight() {
        return null;
    }

    @Nullable
    default IntProvider getVerticalSize() {
        return null;
    }

    double getMinSpawnTreshold();

    double getMaxSpawnTreshold();

    @NoArgsConstructor
    public static class BlobStrata implements SurfaceRules.RuleSource
    {
        public static List<IStrataLayer> LAYERS;
        public static BlobStrata INSTANCE = new BlobStrata();
        public static final KeyDispatchDataCodec<BlobStrata> CODEC = KeyDispatchDataCodec.of(
                RecordCodecBuilder.create(instance -> instance.group(
                        IStrataLayer.CODEC.optionalFieldOf("layer", null).forGetter(it -> it.layer)
                ).apply(instance, BlobStrata::new))
        );

        @Nullable
        private IStrataLayer layer;
        private boolean hasSetLayer = false;
        @Nullable
        public DensityFunction strata3d;
        @Nullable
        public RandomSource random;

        public BlobStrata(IStrataLayer layer) {
            this.layer = layer;
            this.hasSetLayer = layer != null;
        }

        @Override
        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
            if (LAYERS == null || LAYERS.size() == 0) {
                LAYERS = WorldGeneratorUtils.STRATA_LAYERS.values().stream()
                        .filter(IStrataLayer::isNatural)
                        .toList();
            }
            if (strata3d == null) {
                strata3d = BuiltinRegistries.DENSITY_FUNCTION.getOrThrow(GTFeatures.BASE_3D_STRATA_NOISE);
            }
            if (random == null) {
                random = RandomSource.create(context.randomState.legacyLevelSeed());
            }
            strata3d = applyNoiseHolder(strata3d, context);
            NormalNoise strata2d = context.randomState.getOrCreateNoise(GTFeatures.STRATA_NOISE);
            return (x, y, z) -> {
                BlockPos currentPos = new BlockPos(x, y, z);
                BlockState current = context.chunk.getBlockState(currentPos);
                if (current.isAir()) {
                    return null;
                }

                IStrataLayer stratum;
                if (layer == null) {
                    NormalNoise typeNoise = context.randomState.getOrCreateNoise(GTFeatures.STRATA_TYPE_NOISE);
                    double i = typeNoise.getValue(x, y, z);
                    //double maxValue = typeNoise.maxValue();
                    //i = Mth.map(i, -maxValue, maxValue, 0, LAYERS.size());

                    stratum = LAYERS.get(Mth.abs((int) Math.floor(i * 100)) % (LAYERS.size() - 1));
                    if (!hasSetLayer) {
                        layer = stratum;
                    }
                    //getStateForPos(strata2d, LAYERS, x, y, z, context.chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z));
                } else {
                    stratum = layer;
                }

                //final double offset = i * 10 + 64;
                //DensityFunction function = strata3d.mapAll(densityFunction -> new DensityFunctions.ShiftedNoise(DensityFunctions.zero(), DensityFunctions.constant(offset), DensityFunctions.zero(), 1, 3, new DensityFunction.NoiseHolder(BuiltinRegistries.NOISE.getOrCreateHolderOrThrow(GTFeatures.STRATA_NOISE), strata2d)));

                double threshold = strata3d.compute(WorldGeneratorUtils.offsetFunctionContext(context.noiseChunk, x, y, z));
                //threshold = Mth.map(threshold, STRATA_3D.minValue(), STRATA_3D.maxValue(), 0, 1);
                //threshold = Mth.clamp(threshold, 0, 1.0);

                if (threshold >= stratum.getMinSpawnTreshold()/* && threshold <= stratum.getMaxSpawnTreshold()/* && stratum.getHeight().resolveY(context.context) >= y//*/) {
                    return stratum.getState().get().get();
                } else if (!hasSetLayer) {
                    layer = null;
                }
                return null;
            };
        }

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }
    }

    public static class LayerStrata implements SurfaceRules.RuleSource {
        public static IStrataLayer[] LAYERS;
        public static final LayerStrata INSTANCE = new LayerStrata();
        public static final KeyDispatchDataCodec<LayerStrata> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));
        public static final ResourceLocation STRATA_RAND_ID = GTCEu.id("strata");

        @Nullable
        private IStrataLayer currentLayer;

        @Override
        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
            if (LAYERS == null || LAYERS.length == 0) {
                LAYERS = WorldGeneratorUtils.STRATA_LAYERS.values().stream()
                        .filter(IStrataLayer::isNatural)
                        .filter(strata -> strata.getHeight() != null)
                        .filter(strata -> strata.getVerticalSize() != null)
                        .toArray(IStrataLayer[]::new);
            }
            NormalNoise noise = context.randomState.getOrCreateNoise(GTFeatures.STRATA_NOISE);
            return (x, y, z) -> {
                BlockState current = context.chunk.getBlockState(new BlockPos(x, y, z));
                if (current.isAir()) {
                    return null;
                }
                if (currentLayer != null) {
                    int chosenY = currentLayer.getHeight().resolveY(context.context);
                    int difference = Math.abs(chosenY - y);
                    int size = currentLayer.getVerticalSize().sample(context.randomState.getOrCreateRandomFactory(STRATA_RAND_ID).at(x, y, z));
                    if (chosenY >= y && size >= Math.abs(noise.getValue(x, y, z)) * difference) {
                        return currentLayer.getState().get().get();
                    } else {
                        currentLayer = null;
                    }
                }

                int i = (int)Math.round(noise.getValue(x, y, z) * 8.0);
                int index = (y + i + LAYERS.length) % LAYERS.length;
                IStrataLayer layer = LAYERS[index];
                while (layer.getHeight().resolveY(context.context) < y) {
                    if (index < LAYERS.length - 1)
                        layer = LAYERS[++index];
                    else
                        layer = LAYERS[0];
                }
                currentLayer = layer;
                return currentLayer.getState().get().get();
            };
        }

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }
    }

    /**
     *
     * @param candidates the candidates to select from
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @param surfaceY the y value of the world surface
     * @return the selected BlockState to place
     */
    @Nonnull
    private static IStrataLayer getStateForPos(NormalNoise noise, @Nonnull List<IStrataLayer> candidates, int x, int y, int z, int surfaceY) {
        double noiseValue = noise.getValue(x, y * 1.0F / surfaceY, z) * 1.1;
        noiseValue = Mth.map(noiseValue, -noise.maxValue(), noise.maxValue(), 0.0, 1.0);
        return candidates.get((int) ((candidates.size() - 1) * noiseValue));
    }

    private static DensityFunction applyNoiseHolder(DensityFunction function, SurfaceRules.Context context) {
        return function.mapAll(new DensityFunction.Visitor() {
            @Override
            public DensityFunction apply(DensityFunction densityFunction) {
                return densityFunction;
            }

            @Override
            public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
                Holder<NormalNoise.NoiseParameters> holder = noiseHolder.noiseData();
                NormalNoise normalNoise = context.randomState.getOrCreateNoise(holder.unwrapKey().orElseThrow());
                return new DensityFunction.NoiseHolder(holder, normalNoise);
            }
        });
    }

}
