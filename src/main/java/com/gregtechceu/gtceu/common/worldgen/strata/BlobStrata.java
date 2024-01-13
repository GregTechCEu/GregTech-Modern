package com.gregtechceu.gtceu.common.worldgen.strata;

import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.core.mixins.NoiseChunkAccessor;
import com.gregtechceu.gtceu.core.mixins.NormalNoiseAccessor;
import com.gregtechceu.gtceu.core.mixins.SurfaceRulesContextAccessor;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@NoArgsConstructor
public class BlobStrata implements SurfaceRules.RuleSource {
    public static List<IStrataLayer> LAYERS;
    public static BlobStrata INSTANCE = new BlobStrata();
    public static final KeyDispatchDataCodec<BlobStrata> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.create(instance -> instance.group(
                    IStrataLayer.CODEC.optionalFieldOf("layer", null).forGetter(it -> it.layer)
            ).apply(instance, BlobStrata::new))
    );

    @Nullable
    private IStrataLayer layer;

    public BlobStrata(@Nullable IStrataLayer layer) {
        this.layer = layer;
    }

    @Override
    public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
        if (LAYERS == null || LAYERS.size() == 0) {
            LAYERS = WorldGeneratorUtils.STRATA_LAYERS.values().stream()
                .filter(IStrataLayer::isNatural)
                .toList();
        }
        DensityFunction strataNoise = GTRegistries.builtinRegistry().registryOrThrow(Registries.DENSITY_FUNCTION).getOrThrow(GTFeatures.BASE_3D_STRATA_NOISE);
        final DensityFunction strata3d = ((NoiseChunkAccessor)((SurfaceRulesContextAccessor)(Object)context).getNoiseChunk()).invokeWrap(strataNoise);
        NormalNoise typeNoise = ((SurfaceRulesContextAccessor)(Object)context).getRandomState().getOrCreateNoise(GTFeatures.STRATA_TYPE_NOISE);
        return (x, y, z) -> {
            IStrataLayer stratum;
            stratum = Objects.requireNonNullElseGet(INSTANCE.layer, () -> getStateForPos(typeNoise, LAYERS, x, y, z));

            double threshold = strata3d.compute(WorldGeneratorUtils.createFunctionContext(x, y ,z));

            if (threshold >= stratum.getMinSpawnTreshold()) {
                return stratum.getState().get().get();
            }
            return null;
        };
    }

    /**
     *
     * @param candidates the candidates to select from
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return the selected BlockState to place
     */
    @Nonnull
    private static IStrataLayer getStateForPos(NormalNoise noise, @Nonnull List<IStrataLayer> candidates, int x, int y, int z) {
        double noiseValue = getNoiseValue(noise, x * 0.05, y * 0.05, z * 0.05);
        return candidates.get(Mth.clamp(Mth.abs((int) ((candidates.size() - 1) * noiseValue)), 0, candidates.size() - 1));
    }

    @SuppressWarnings("deprecation")
    private static double getNoiseValue(NormalNoise noise, double x, double y, double z) {
        double valueFactor = ((NormalNoiseAccessor)noise).getValueFactor();
        PerlinNoise first = ((NormalNoiseAccessor)noise).getFirst();
        PerlinNoise second = ((NormalNoiseAccessor)noise).getSecond();
        double d = x * 1.0181268882175227;
        double e = y * 1.0181268882175227;
        double f = z * 1.0181268882175227;
        return (first.getValue(x, y, z, 1, 0, false) + second.getValue(d, e, f, 1, 0, false)) * valueFactor;
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
        return CODEC;
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
                NormalNoise normalNoise = ((SurfaceRulesContextAccessor)(Object)context).getRandomState().getOrCreateNoise(holder.unwrapKey().orElseThrow());
                return new DensityFunction.NoiseHolder(holder, normalNoise);
            }
        });
    }
}
