package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.effect.GTPoisonEffect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GTMobEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,
            GTCEu.MOD_ID);

    public static final RegistryObject<GTPoisonEffect> WEAK_POISON = MOB_EFFECTS.register("weak_poison",
            () -> new GTPoisonEffect(MobEffectCategory.HARMFUL, 0x6D7917));

    public static void init(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }
}
