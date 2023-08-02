package com.gregtechceu.gtceu.api.data.worldgen.strata;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.function.Supplier;

public interface IStrataLayer extends StringRepresentable {
    Codec<IStrataLayer> CODEC = ExtraCodecs.stringResolverCodec(StringRepresentable::getSerializedName, WorldGeneratorUtils.STRATA_LAYERS::get);

    boolean isNatural();

    Supplier<Supplier<BlockState>> getState();

    Material getMaterial();

    TagPrefix getTagPrefix();

    VerticalAnchor getHeight();



    public class StrataNoise implements SurfaceRules.RuleSource
    {
        public static final IStrataLayer[] LAYERS = WorldGeneratorUtils.STRATA_LAYERS.values().toArray(IStrataLayer[]::new);

        public static final StrataNoise INSTANCE = new StrataNoise();

        static final KeyDispatchDataCodec<StrataNoise> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }

        @Override
        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
            NormalNoise noise = context.randomState.getOrCreateNoise(Noises.CLAY_BANDS_OFFSET);
            return (x, y, z) -> {
                int i = (int)Math.round(noise.getValue(x, 0.0, z) * 4.0);
                int index = (y + i + LAYERS.length) % LAYERS.length;
                IStrataLayer layer = LAYERS[index];
                while (layer.getHeight().resolveY(context.context) > y && index > LAYERS.length - 1) {
                    layer = LAYERS[++index];
                }
                return layer.getState().get().get();
            };
        }
    }

}
