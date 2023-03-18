package com.gregtechceu.gtceu.api.registry.fabric;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRegistriesImpl
 */
public class GTRegistriesImpl {
    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        return Registry.register(registry, name, value);
    }
}
