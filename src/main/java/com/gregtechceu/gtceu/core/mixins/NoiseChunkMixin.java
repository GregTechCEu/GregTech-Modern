package com.gregtechceu.gtceu.core.mixins;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.data.worldgen.strata.StrataGenerationType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.common.worldgen.strata.BlobStrata;
import com.gregtechceu.gtceu.common.worldgen.strata.LayerStrata;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;

@Mixin(NoiseChunk.class)
public abstract class NoiseChunkMixin {

    @Shadow
    protected abstract DensityFunction wrap(DensityFunction densityFunction);

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList$Builder;build()Lcom/google/common/collect/ImmutableList;", remap = false))
    private ImmutableList<NoiseChunk.BlockStateFiller> gtceu$addStrataFiller(ImmutableList<NoiseChunk.BlockStateFiller> original,
                                                                                    int cellCountXZ, RandomState random, int firstNoiseX, int firstNoiseZ, NoiseSettings noiseSettings, DensityFunctions.BeardifierOrMarker beardifier, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker, Blender blendifier) {
        ArrayList<NoiseChunk.BlockStateFiller> newList = new ArrayList<>(original);
        StrataGenerationType type = ConfigHolder.INSTANCE.worldgen.strataGeneration;
        if (type != StrataGenerationType.NONE) {
            switch (type) {
                case LAYER -> newList.add(LayerStrata.create(random, random.getOrCreateNoise(GTFeatures.STRATA_NOISE)));
                case BLOB -> newList.add(BlobStrata.create(random, this.wrap(GTRegistries.builtinRegistry().registry(Registries.DENSITY_FUNCTION).get().getOrThrow(GTFeatures.BASE_3D_STRATA_NOISE))));
            }
        }
        return ImmutableList.copyOf(newList);
    }
}
