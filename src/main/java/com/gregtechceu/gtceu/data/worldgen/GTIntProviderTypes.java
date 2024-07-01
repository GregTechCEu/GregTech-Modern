package com.gregtechceu.gtceu.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.RubberTreeChanceWeightedListInt;

import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.IntProviderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTIntProviderTypes {

    public static final DeferredRegister<IntProviderType<?>> INT_PROVIDER_TYPES = DeferredRegister
            .create(Registries.INT_PROVIDER_TYPE, GTCEu.MOD_ID);

    public static final DeferredHolder<IntProviderType<?>, IntProviderType<RubberTreeChanceWeightedListInt>> RUBBER_TREE_CHANCE = INT_PROVIDER_TYPES
            .register("rubber_tree_chance", () -> () -> RubberTreeChanceWeightedListInt.CODEC);

    public static void init(IEventBus modBus) {
        INT_PROVIDER_TYPES.register(modBus);
    }
}
