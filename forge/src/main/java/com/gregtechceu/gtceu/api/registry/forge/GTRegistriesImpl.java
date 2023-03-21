package com.gregtechceu.gtceu.api.registry.forge;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRegistriesImpl
 */
public class GTRegistriesImpl {
    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        if (registry == Registry.RECIPE_TYPE) {
            ForgeRegistries.RECIPE_TYPES.register(name, (RecipeType<?>) value);
        } else if (registry == Registry.RECIPE_SERIALIZER) {
            ForgeRegistries.RECIPE_SERIALIZERS.register(name, (RecipeSerializer<?>) value);
        } else if (registry == Registry.FEATURE) {
            ForgeRegistries.FEATURES.register(name, (Feature<?>) value);
        } else {
            return Registry.register(registry, name, value);
        }
        return value;
    }
}
