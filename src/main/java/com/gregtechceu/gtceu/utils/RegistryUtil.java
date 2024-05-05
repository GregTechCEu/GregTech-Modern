package com.gregtechceu.gtceu.utils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RegistryUtil {
    private RegistryUtil() {
    }

    public static <T> List<ResourceKey<T>> resolveResourceKeys(ResourceKey<Registry<T>> registryKey, String... locations) {
        return Arrays.stream(locations)
            .map(location -> ResourceKey.create(registryKey, new ResourceLocation(location)))
            .toList();
    }
}
