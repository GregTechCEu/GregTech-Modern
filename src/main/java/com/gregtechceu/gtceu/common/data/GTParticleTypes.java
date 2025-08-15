package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.particle.HazardParticleOptions;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.mojang.serialization.Codec;

public class GTParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister
            .create(Registries.PARTICLE_TYPE, GTCEu.MOD_ID);

    public static final RegistryObject<ParticleType<HazardParticleOptions>> HAZARD_PARTICLE = PARTICLE_TYPES
            .register("hazard", () -> new ParticleType<>(false, HazardParticleOptions.DESERIALIZER) {

                @Override
                public Codec<HazardParticleOptions> codec() {
                    return HazardParticleOptions.CODEC;
                }
            });
    public static final RegistryObject<SimpleParticleType> MUFFLER_PARTICLE = PARTICLE_TYPES
            .register("muffler", () -> new SimpleParticleType(false));

    public static void init(IEventBus modBus) {
        PARTICLE_TYPES.register(modBus);
    }
}
