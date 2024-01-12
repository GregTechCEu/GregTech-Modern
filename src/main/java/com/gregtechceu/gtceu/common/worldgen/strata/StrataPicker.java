package com.gregtechceu.gtceu.common.worldgen.strata;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.SurfaceRulesContextAccessor;
import com.gregtechceu.gtceu.core.mixins.SurfaceSystemAccessor;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class StrataPicker implements SurfaceRules.RuleSource {
    public static final LayerStrata INSTANCE = new LayerStrata();
    public static final KeyDispatchDataCodec<LayerStrata> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));
    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
        return switch (ConfigHolder.INSTANCE.worldgen.strataGeneration) {
            case LAYER -> LayerStrata.INSTANCE.apply(context);
            case BLOB -> BlobStrata.INSTANCE.apply(context);
            case NONE -> (x, y, z) -> ((SurfaceSystemAccessor)((SurfaceRulesContextAccessor)(Object)context).getSystem()).getDefaultBlock();
        };
    }
}
