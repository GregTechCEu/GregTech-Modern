package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTConfiguredFeatures;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeatureUtils.class)
public class FeatureUtilsMixin {

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void gtceu$injectFeatures(BootstapContext<ConfiguredFeature<?, ?>> context, CallbackInfo callbackInfo) {
        FeatureUtils.register(context, GTConfiguredFeatures.ORE, GTFeatures.ORE, new GTOreFeatureConfiguration());
        for (var entry : GTRegistries.ORE_VEINS.entries()) {
            ResourceLocation id = entry.getKey();
            var generator = entry.getValue().getVeinGenerator();
            if (generator != null && generator != GTOreFeatureEntry.NoopVeinGenerator.INSTANCE) {
                generator.build();
                GTOreFeatureConfiguration config = new GTOreFeatureConfiguration(entry.getValue());
                FeatureUtils.register(context, ResourceKey.create(Registries.CONFIGURED_FEATURE, id), GTFeatures.ORE, config);
            }
        }
    }

}
