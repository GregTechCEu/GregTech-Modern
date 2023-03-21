package com.gregtechceu.gtceu.common.data.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTOresImpl
 */
public class GTOresImpl {
    private static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE_REGISTER = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, GTCEu.MOD_ID);
    private static final DeferredRegister<PlacedFeature> PLACED_FEATURE_REGISTER = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, GTCEu.MOD_ID);
    private static final DeferredRegister<BiomeModifier> BIOME_MODIFIER_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIERS, GTCEu.MOD_ID);

    public static void init(IEventBus modEventBus) {
        CONFIGURED_FEATURE_REGISTER.register(modEventBus);
        PLACED_FEATURE_REGISTER.register(modEventBus);
        BIOME_MODIFIER_REGISTER.register(modEventBus);
    }

    public static void register() {
        for (var entry : GTOreFeatureEntry.ALL.entrySet()) {
            ResourceLocation id = entry.getKey();
            var datagenExt = entry.getValue().datagenExt();
            if (datagenExt != null) {
                CONFIGURED_FEATURE_REGISTER.register(id.getPath(), () -> datagenExt.createConfiguredFeature(BuiltinRegistries.ACCESS));
                PLACED_FEATURE_REGISTER.register(id.getPath(), () -> datagenExt.createPlacedFeature(BuiltinRegistries.ACCESS));
                BIOME_MODIFIER_REGISTER.register(id.getPath(), () -> {
                    Registry<Biome> biomeRegistry = BuiltinRegistries.ACCESS.registryOrThrow(Registry.BIOME_REGISTRY);
                    Registry<PlacedFeature> featureRegistry = BuiltinRegistries.ACCESS.registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
                    HolderSet<Biome> biomes = new HolderSet.Named<>(biomeRegistry, datagenExt.biomeTag);
                    Holder<PlacedFeature> featureHolder = featureRegistry.getOrCreateHolderOrThrow(ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, id));
                    return new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                            biomes,
                            HolderSet.direct(featureHolder),
                            GenerationStep.Decoration.UNDERGROUND_ORES
                    );
                });

            }
        }
    }
}
