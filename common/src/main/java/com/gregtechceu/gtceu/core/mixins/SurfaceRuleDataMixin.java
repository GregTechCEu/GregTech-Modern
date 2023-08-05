package com.gregtechceu.gtceu.core.mixins;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer;
import com.gregtechceu.gtceu.common.block.StoneTypes;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SurfaceRuleData.class)
public class SurfaceRuleDataMixin {

    @Inject(method = "overworldLike(ZZZ)Lnet/minecraft/world/level/levelgen/SurfaceRules$RuleSource;",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList$Builder;build()Lcom/google/common/collect/ImmutableList;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void gtceu$injectStrata(boolean abovePreliminarySurface, boolean bedrockRoof, boolean bedrockFloor, CallbackInfoReturnable<SurfaceRules.RuleSource> cir,
                                           SurfaceRules.ConditionSource conditionSource, SurfaceRules.ConditionSource conditionSource2, SurfaceRules.ConditionSource conditionSource3, SurfaceRules.ConditionSource conditionSource4, SurfaceRules.ConditionSource conditionSource5, SurfaceRules.ConditionSource conditionSource6, SurfaceRules.ConditionSource conditionSource7, SurfaceRules.ConditionSource conditionSource8, SurfaceRules.ConditionSource conditionSource9, SurfaceRules.ConditionSource conditionSource10, SurfaceRules.ConditionSource conditionSource11, SurfaceRules.ConditionSource conditionSource12, SurfaceRules.ConditionSource conditionSource13, SurfaceRules.RuleSource ruleSource, SurfaceRules.RuleSource ruleSource2, SurfaceRules.RuleSource ruleSource3, SurfaceRules.ConditionSource conditionSource14, SurfaceRules.ConditionSource conditionSource15, SurfaceRules.RuleSource ruleSource4, SurfaceRules.RuleSource ruleSource5, SurfaceRules.RuleSource ruleSource6, SurfaceRules.RuleSource ruleSource7, SurfaceRules.RuleSource ruleSource8, SurfaceRules.ConditionSource conditionSource16, SurfaceRules.ConditionSource conditionSource17, SurfaceRules.ConditionSource conditionSource18, SurfaceRules.RuleSource ruleSource9,
                                           ImmutableList.Builder<SurfaceRules.RuleSource> builder) {
        if (ConfigHolder.INSTANCE == null) ConfigHolder.init();
        if (WorldGeneratorUtils.STRATA_LAYERS.size() == 0) StoneTypes.init();
        switch (ConfigHolder.INSTANCE.worldgen.strataGeneration) {
            case BLOB -> {
                builder.add(IStrataLayer.BlobStrata.INSTANCE);
                //for (IStrataLayer layer : WorldGeneratorUtils.STRATA_LAYERS.values()) {
                //    builder.add(new IStrataLayer.BlobStrata(layer));
                //}
            }
            case LAYER -> builder.add(IStrataLayer.LayerStrata.INSTANCE);
        }
    }

}
