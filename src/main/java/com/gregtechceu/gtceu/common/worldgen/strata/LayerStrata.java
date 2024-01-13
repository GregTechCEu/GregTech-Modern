package com.gregtechceu.gtceu.common.worldgen.strata;

import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.core.mixins.SurfaceRulesContextAccessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LayerStrata implements SurfaceRules.RuleSource {
    public static IStrataLayer[] LAYERS;
    public static final LayerStrata INSTANCE = new LayerStrata();
    public static final KeyDispatchDataCodec<LayerStrata> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

    @Nullable
    private static IStrataLayer currentLayer;

    @Override
    public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
        if (LAYERS == null || LAYERS.length == 0) {
            LAYERS = WorldGeneratorUtils.STRATA_LAYERS.values().stream()
                .filter(IStrataLayer::isNatural)
                .filter(strata -> strata.getHeight() != null)
                .filter(strata -> strata.getSize() != null)
                .toArray(IStrataLayer[]::new);
        }
        final NormalNoise noise = ((SurfaceRulesContextAccessor)(Object)context).getRandomState().getOrCreateNoise(GTFeatures.STRATA_NOISE);
        final PositionalRandomFactory randomFactory = ((SurfaceRulesContextAccessor)(Object)context).getRandomState().getOrCreateRandomFactory(GTFeatures.LAYER_STRATA.location());
        return (x, y, z) -> {
            RandomSource random = randomFactory.at(x, y, z);
            if (currentLayer != null) {
                int chosenY = currentLayer.getHeight().resolveY(((SurfaceRulesContextAccessor)(Object)context).getContext());
                int difference = Math.abs(chosenY - y);
                int size = currentLayer.getSize().sample(random);
                if (chosenY >= y && size >= Math.abs(noise.getValue(x, y, z)) * difference) {
                    return currentLayer.getState().get().get();
                }
            }

            int i = (int)Math.round(noise.getValue(x, y, z) * 8.0);
            int index = Math.abs((y + i + LAYERS.length) % LAYERS.length);
            //while (layer.getHeight().resolveY(context.context) > y) {
            //    if (index < LAYERS.length - 1)
            //        layer = LAYERS[++index];
            //    else
            //        layer = LAYERS[0];
            //}
            currentLayer = LAYERS[index];
            return currentLayer.getState().get().get();
        };
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
        return CODEC;
    }
}
