package com.gregtechceu.gtceu.common.data.forge;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GTFeaturesImpl
 */
public class GTFeaturesImpl {
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE_REGISTER = DeferredRegister.create(Registries.CONFIGURED_FEATURE, GTCEu.MOD_ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURE_REGISTER = DeferredRegister.create(Registries.PLACED_FEATURE, GTCEu.MOD_ID);
    public static final DeferredRegister<BiomeModifier> BIOME_MODIFIER_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIERS, GTCEu.MOD_ID);

    public static void init(IEventBus modEventBus) {
        CONFIGURED_FEATURE_REGISTER.register(modEventBus);
        PLACED_FEATURE_REGISTER.register(modEventBus);
        BIOME_MODIFIER_REGISTER.register(modEventBus);
    }

    public static void register() {
        // no-op
    }
}
