package com.gregtechceu.gtceu.core.mixins;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer;
import com.gregtechceu.gtceu.api.data.worldgen.strata.StrataGenerationType;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin {



    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList$Builder;add(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;", ordinal = 0))
    public ImmutableList.Builder<NoiseChunk.BlockStateFiller> gtceu$addStrataNoise(ImmutableList.Builder<NoiseChunk.BlockStateFiller> builder, Object element,
                                                                                   int i, RandomState randomState, int j, int k, NoiseSettings noiseSettings, DensityFunctions.BeardifierOrMarker beardifierOrMarker, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker, Blender blender) {
        DensityFunction strataDensity = BuiltinRegistries.DENSITY_FUNCTION.getOrThrow(GTFeatures.BASE_3D_STRATA_NOISE);
        strataDensity = strataDensity.mapAll(new DensityFunction.Visitor() {
            @Override
            public DensityFunction apply(DensityFunction densityFunction) {
                return densityFunction;
            }

            @Override
            public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
                Holder<NormalNoise.NoiseParameters> holder = noiseHolder.noiseData();
                NormalNoise normalNoise = randomState.getOrCreateNoise(holder.unwrapKey().orElseThrow());
                return new DensityFunction.NoiseHolder(holder, normalNoise);
            }
        });
        builder.add(IStrataLayer.BlobStrata.create(strataDensity, randomState.router().depth(), randomState));
//        if (ConfigHolder.INSTANCE.worldgen.strataGeneration == StrataGenerationType.BLOB) {
//            for (IStrataLayer layer : WorldGeneratorUtils.STRATA_LAYERS.values()) {
//                if (!layer.isNatural()) continue;
//                builder.add(IStrataLayer.BlobStrata.create(strataDensity, randomState.router().depth(), randomState));
//            }
//        }

        builder.add((NoiseChunk.BlockStateFiller) element);
        return builder;
    }
}
