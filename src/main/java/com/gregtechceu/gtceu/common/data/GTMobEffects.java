package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.effect.GTPoisonEffect;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTMobEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT,
            GTCEu.MOD_ID);

    public static final DeferredHolder<MobEffect, GTPoisonEffect> WEAK_POISON = MOB_EFFECTS.register("weak_poison",
            () -> new GTPoisonEffect(MobEffectCategory.HARMFUL, 0x6D7917));

    public static void init(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }
}
