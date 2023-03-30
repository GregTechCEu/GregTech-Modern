package com.gregtechceu.gtceu.api.registry.forge;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static net.minecraft.core.Registry.RECIPE_TYPE_REGISTRY;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRegistriesImpl
 */
public class GTRegistriesImpl {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER_TYPE = DeferredRegister.create(Registry.TRUNK_PLACER_TYPE_REGISTRY, GTCEu.MOD_ID);

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        ResourceKey<?> registryKey = registry.key();

        if (registryKey == RECIPE_TYPE_REGISTRY) {
            ForgeRegistries.RECIPE_TYPES.register(name, (RecipeType<?>) value);
        } else if (registryKey == Registry.RECIPE_SERIALIZER_REGISTRY) {
            ForgeRegistries.RECIPE_SERIALIZERS.register(name, (RecipeSerializer<?>) value);
        } else if (registryKey == Registry.FEATURE_REGISTRY) {
            ForgeRegistries.FEATURES.register(name, (Feature<?>) value);
        } else if (registryKey == Registry.FOLIAGE_PLACER_TYPE_REGISTRY) {
            ForgeRegistries.FOLIAGE_PLACER_TYPES.register(name, (FoliagePlacerType<?>)value);
        } else if (registryKey == Registry.TRUNK_PLACER_TYPE_REGISTRY) {
            TRUNK_PLACER_TYPE.register(name.getPath(), () -> (TrunkPlacerType<?>)value);
        } else {
            return Registry.register(registry, name, value);
        }

        return value;
    }

    public static void init(IEventBus eventBus) {
        TRUNK_PLACER_TYPE.register(eventBus);
    }
}
