package com.gregtechceu.gtceu.api.registry.forge;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote GTRegistriesImpl
 */
public class GTRegistriesImpl {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER_TYPE = DeferredRegister.create(Registry.TRUNK_PLACER_TYPE_REGISTRY, GTCEu.MOD_ID);

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        if (registry == Registry.RECIPE_TYPE) {
            ForgeRegistries.RECIPE_TYPES.register(name, (RecipeType<?>) value);
        } else if (registry == Registry.RECIPE_SERIALIZER) {
            ForgeRegistries.RECIPE_SERIALIZERS.register(name, (RecipeSerializer<?>) value);
        } else if (registry == Registry.FEATURE) {
            ForgeRegistries.FEATURES.register(name, (Feature<?>) value);
        } else if (registry == Registry.TRUNK_PLACER_TYPES) {
            TRUNK_PLACER_TYPE.register(name.getPath(), () -> (TrunkPlacerType<?>)value);
        } else if (registry == Registry.FOLIAGE_PLACER_TYPES) {
            ForgeRegistries.FOLIAGE_PLACER_TYPES.register(name, (FoliagePlacerType<?>)value);
        } else {
            return Registry.register(registry, name, value);
        }
        return value;
    }

    public static void init(IEventBus eventBus) {
        TRUNK_PLACER_TYPE.register(eventBus);
    }
}
