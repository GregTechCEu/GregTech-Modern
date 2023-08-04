package com.gregtechceu.gtceu.api.data.worldgen.strata;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer.LayerStrata.STRATA_RAND_ID;

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

    public class BlobStrataNoise implements SurfaceRules.RuleSource
    {
        public static IStrataLayer[] LAYERS;
        public static DensityFunction STRATA_3D;
        public static final KeyDispatchDataCodec<BlobStrataNoise> CODEC = KeyDispatchDataCodec.of(RecordCodecBuilder.create(instance -> instance.group(
                IStrataLayer.CODEC.fieldOf("strata").forGetter(it -> it.stratum)
        ).apply(instance, BlobStrataNoise::new)));

        private final IStrataLayer stratum;

        public BlobStrataNoise(IStrataLayer stratum) {
            this.stratum = stratum;
        }

        @Override
        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
            if (LAYERS == null || LAYERS.length == 0) {
                LAYERS = WorldGeneratorUtils.STRATA_LAYERS.values().stream()
                        .filter(IStrataLayer::isNatural)
                        .filter(strata -> strata.getHeight() != null)
                        .filter(strata -> strata.getVerticalSize() != null)
                        .toArray(IStrataLayer[]::new);
            }
            if (STRATA_3D == null) {
                STRATA_3D = BuiltinRegistries.DENSITY_FUNCTION.getOrThrow(GTFeatures.BASE_3D_STRATA_NOISE);
                STRATA_3D = STRATA_3D.mapAll(new DensityFunction.Visitor() {
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
            MutableInt startY = new MutableInt();
          return (x, y, z) -> {
                if (startY.getValue() < context.chunk.getMinBuildHeight()) {
                    startY.setValue(y);
                }
                int distance = Mth.abs(y - startY.getValue());
                if (distance < 2) { // very arbitrary "feels good" number
                    distance = 1;
                }

                BlockState current = context.chunk.getBlockState(new BlockPos(x, y, z));
                if (current.isAir()) {
                    return null;
                }

                double threshold = STRATA_3D.compute(context.noiseChunk);
                threshold = Mth.map(threshold, -1, 1, 0, 1);
                threshold = Mth.clamp(threshold, 0, 1.0 / (distance));
                if (threshold >= stratum.getMinSpawnTreshold()) {
                  return stratum.getState().get().get();
                }
                return null;
            };
        }

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }
    }

    public class LayerStrata implements SurfaceRules.RuleSource {
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

    public static class BlobStrata {
        public static IStrataLayer[] LAYERS;
        public static final ResourceLocation STRATA_RAND_ID = GTCEu.id("strata");

        public static NoiseChunk.BlockStateFiller create(DensityFunction strataDensity, DensityFunction depth, RandomState randomState) {
            if (LAYERS == null || LAYERS.length == 0) {
                LAYERS = WorldGeneratorUtils.STRATA_LAYERS.values().stream()
                        .filter(IStrataLayer::isNatural)
                        .filter(strata -> strata.getHeight() != null)
                        .filter(strata -> strata.getVerticalSize() != null)
                        .toArray(IStrataLayer[]::new);
            }

            final AtomicReference<IStrataLayer> currentLayer = new AtomicReference<>();
            final AtomicInteger originalY = new AtomicInteger(Integer.MIN_VALUE);
            return functionContext -> {
                double depthed = depth.compute(functionContext);
                if (depthed >= 0.1 && depthed <= 0.75) {
                    NormalNoise noise = randomState.getOrCreateNoise(GTFeatures.STRATA_NOISE);

                    int x = functionContext.blockX();
                    int y = functionContext.blockY();
                    int z = functionContext.blockZ();

                    if (currentLayer.get() != null) {
                        originalY.compareAndSet(Integer.MIN_VALUE, y);
                        int chosenY = originalY.get();

                        int difference = Math.abs(chosenY - y);
                        int size = currentLayer.get().getVerticalSize().sample(randomState.getOrCreateRandomFactory(STRATA_RAND_ID).at(x, y, z));
                        if (size >= Math.abs(noise.getValue(x, y, z)) * difference) {
                            return currentLayer.get().getState().get().get();
                        } else {
                            originalY.set(Integer.MIN_VALUE);
                            currentLayer.set(null);
                        }
                    }

                    double threshold = strataDensity.compute(functionContext);
                    int i = (int)Math.round(noise.getValue(x, y, z) * 8.0);
                    int index = Math.abs((y + i + LAYERS.length) % LAYERS.length);
                    IStrataLayer layer = LAYERS[index];
                    if (threshold >= layer.getMinSpawnTreshold()/* && threshold <= layer.getMaxSpawnTreshold()*/) {
                        currentLayer.set(layer);
                        return layer.getState().get().get();
                    }
                }
                return null;
            };
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
        noiseValue = Mth.map(noiseValue, -1, 1, 0.0, 1.0);
        return candidates.get((int) ((candidates.size() - 1) * noiseValue));
    }

}
