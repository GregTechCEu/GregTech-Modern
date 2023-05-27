package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlacedFeature.class)
public abstract class PlacedFeatureMixin {

    @Shadow @Final private Holder<ConfiguredFeature<?, ?>> feature;

    @Shadow @Final @Mutable
    private List<PlacementModifier> placement;

    @Inject(method = "placeWithContext", at = @At(value = "HEAD"))
    public void gtceu$injectPlaceFeature(PlacementContext context, RandomSource source, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (feature.value().config() instanceof GTOreFeatureConfiguration configuration) {
            GTOreFeatureEntry entry = configuration.getEntry(source);
            placement = entry.modifiers;
            configuration.setEntry(entry);
        }
    }
}
