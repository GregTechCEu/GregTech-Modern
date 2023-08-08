package com.gregtechceu.gtceu.common.worldgen.strata;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.mojang.serialization.MapCodec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.state.BlockState;
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
            if (currentLayer != null && currentLayer.getHeight() != null && currentLayer.getVerticalSize() != null) {
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
            while (layer.getHeight() != null && layer.getHeight().resolveY(context.context) < y) {
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
