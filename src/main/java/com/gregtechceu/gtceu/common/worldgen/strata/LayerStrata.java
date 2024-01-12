package com.gregtechceu.gtceu.common.worldgen.strata;

import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.core.mixins.SurfaceRulesContextAccessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
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

    public static NoiseChunk.BlockStateFiller create(RandomState state, NormalNoise strataNoise) {
        if (LAYERS == null || LAYERS.length == 0) {
            LAYERS = WorldGeneratorUtils.STRATA_LAYERS.values().stream()
                    .filter(IStrataLayer::isNatural)
                    .filter(strata -> strata.getHeight() != null)
                    .filter(strata -> strata.getSize() != null)
                    .toArray(IStrataLayer[]::new);
        }
        return (ctx) -> {
            int x = ctx.blockX();
            int y = ctx.blockY();
            int z = ctx.blockZ();
            RandomSource random = state.getOrCreateRandomFactory(GTFeatures.LAYER_STRATA.location()).at(x, y, z);

            if (currentLayer != null) {
                //int chosenY = currentLayer.getHeight().resolveY(new WorldGenerationContext(region.getLevel().getChunkSource().getGenerator(), region));
                int chosenY = y + 20; // TODO look for a solution to not having a worldGenerationContext here.
                int difference = Math.abs(chosenY - y);
                int size = currentLayer.getSize().sample(random);
                if (chosenY >= y && size >= Math.abs(strataNoise.getValue(x, y, z)) * difference) {
                    return currentLayer.getState().get().get();
                }
            }

//            int i = (int)Math.round(strataNoise.getValue(x, y, z) * 8.0);
//            int index = Math.abs((y + i + LAYERS.length) % LAYERS.length);
            //while (layer.getHeight().resolveY(context.context) > y) {
            //    if (index < LAYERS.length - 1)
            //        layer = LAYERS[++index];
            //    else
            //        layer = LAYERS[0];
            //}
//            currentLayer = LAYERS[index];
//            return currentLayer.getState().get().get();
            return null;
        };
    }

    @Override
    public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
        if (LAYERS == null || LAYERS.length == 0) {
            LAYERS = WorldGeneratorUtils.STRATA_LAYERS.values().stream()
                .filter(IStrataLayer::isNatural)
                .filter(strata -> strata.getHeight() != null)
                .filter(strata -> strata.getSize() != null)
                .toArray(IStrataLayer[]::new);
        }
        NormalNoise noise = ((SurfaceRulesContextAccessor)(Object)context).getRandomState().getOrCreateNoise(GTFeatures.STRATA_NOISE);
        return (x, y, z) -> {
            RandomSource random = ((SurfaceRulesContextAccessor)(Object)context).getRandomState().getOrCreateRandomFactory(GTFeatures.LAYER_STRATA.location()).at(x, y, z);
            BlockState current = ((SurfaceRulesContextAccessor)(Object)context).getChunk().getBlockState(new BlockPos(x, y, z));
            if (current.isAir()) {
                return null;
            }
            if (currentLayer != null) {
                int chosenY = currentLayer.getHeight().resolveY(((SurfaceRulesContextAccessor)(Object)context).getContext());
                int difference = Math.abs(chosenY - y);
                int size = currentLayer.getSize().sample(random);
                if (chosenY >= y && size >= Math.abs(noise.getValue(x, y, z)) * difference) {
                    return currentLayer.getState().get().get();
                }
            }

//            int i = (int)Math.round(strataNoise.getValue(x, y, z) * 8.0);
//            int index = Math.abs((y + i + LAYERS.length) % LAYERS.length);
            //while (layer.getHeight().resolveY(context.context) > y) {
            //    if (index < LAYERS.length - 1)
            //        layer = LAYERS[++index];
            //    else
            //        layer = LAYERS[0];
            //}
//            currentLayer = LAYERS[index];
//            return currentLayer.getState().get().get();
            return null;
        };
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
        return CODEC;
    }
}
